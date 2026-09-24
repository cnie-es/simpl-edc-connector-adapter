package eu.europa.ec.simpl.edcconnectoradapter.service.consumer.transferprocess;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import eu.europa.ec.simpl.data1.common.adapter.connector.model.transfer.TransferProcess;
import eu.europa.ec.simpl.data1.common.adapter.connector.model.transfer.TransferProcessId;
import eu.europa.ec.simpl.data1.common.adapter.connector.model.transfer.TransferRequest;
import eu.europa.ec.simpl.data1.common.util.RemoteServiceUtil;
import eu.europa.ec.simpl.edcconnectoradapter.client.authprovider.ParticipantPingClient;
import eu.europa.ec.simpl.edcconnectoradapter.client.edcconnector.EDCConnectorTransferClient;
import eu.europa.ec.simpl.edcconnectoradapter.constant.Constants;
import eu.europa.ec.simpl.edcconnectoradapter.constant.TransferBusinessOperation;
import eu.europa.ec.simpl.edcconnectoradapter.enumeration.EDCErrorType;
import eu.europa.ec.simpl.edcconnectoradapter.logging.TransferBusinessLogger;
import eu.europa.ec.simpl.edcconnectoradapter.model.edc.common.response.EdcAcknowledgementId;
import eu.europa.ec.simpl.edcconnectoradapter.model.edc.contract.response.EdcContractAgreement;
import eu.europa.ec.simpl.edcconnectoradapter.model.edc.transfer.TransferRecordLog;
import eu.europa.ec.simpl.edcconnectoradapter.model.edc.transfer.request.EdcTransferRequest;
import eu.europa.ec.simpl.edcconnectoradapter.model.edc.transfer.response.EdcTransferProcess;
import eu.europa.ec.simpl.edcconnectoradapter.service.consumer.contractnegotiation.ContractNegotiationService;
import eu.europa.ec.simpl.edcconnectoradapter.util.ModelUtil;
import eu.europa.ec.simpl.edcconnectoradapter.util.PathUtil;
import feign.FeignException;
import jakarta.annotation.PostConstruct;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

@Profile({ "build", "test" })
@Log4j2
@Service
@RequiredArgsConstructor
public class TransferProcessServiceImpl implements TransferProcessService {

    protected static final String COMPLETED_STATUS = "COMPLETED";

    private static final String DEPROVISIONING_ENABLED_PROPERTY_NAME = "deprovisioningEnabled";

    @Value("${edc-connector.base-url}")
    private String edcConnectorBaseUrl;

    @Value("${edc-connector.api-key.value}")
    private String edcConnectorApiKeyValue;

    // own participant identity (consumer side), used to enrich transfer business logs with the consumerId
    @Value("${edc-connector.participant.id:}")
    private String participantId;

    protected final EDCConnectorTransferClient edcConnectorTransferClient;
    protected final ContractNegotiationService contractNegotiationService;
    protected final TransferBusinessLogger transferBusinessLogger;
    protected final ParticipantPingClient participantPingClient;

    protected URI edcConsumerManagementUrl;

    // EDC transfer states that close the transfer; the cached enrichment is evicted once one is reached
    private static final Set<String> TERMINAL_STATES = Set.of("COMPLETED", "TERMINATED", "DEPROVISIONED");

    // canonical participant identity resolved once at initiation, reused on every state-change emission so each
    // emitted record carries the uuids/names even when they cannot be re-resolved at poll time
    private final Map<String, TransferEnrichment> enrichmentByTransfer = new ConcurrentHashMap<>();

    // the provider endpoint is kept alongside the identity so a half-resolved identity (e.g. the ping failed at
    // initiation) can be resolved again on a later state change, where the transfer process alone does not carry it
    private record TransferEnrichment(
            String providerEndpoint,
            String providerParticipantId,
            String providerName,
            String consumerParticipantId,
            String consumerName) {

        boolean isProviderResolved() {
            return providerParticipantId != null && providerName != null;
        }

        boolean isConsumerResolved() {
            return consumerParticipantId != null && consumerName != null;
        }
    }

    @PostConstruct
    public void init() {
        edcConsumerManagementUrl = URI.create(PathUtil.checkSuffix(Constants.EDC_MANAGEMENT_PATH, edcConnectorBaseUrl));
        log.info("init(): edcConsumerManagementUrl='{}'", edcConsumerManagementUrl);
    }

    @Override
    public TransferProcessId startTransfer(TransferRequest transferRequest) {
        String transferType = getTransferType(transferRequest);

        String providerProtocolUrl = PathUtil.checkSuffix(Constants.EDC_PROTOCOL_PATH,
                transferRequest.getProviderEndpoint());
        EdcTransferRequest edcTransferRequest = EdcTransferRequest.builder()
                .counterPartyAddress(providerProtocolUrl)
                .contractId(transferRequest.getContractId())
                .transferType(transferType)
                .dataDestination(normalizeDataDestination(transferRequest.getDataDestination()))
                .build();
        try {
            log.debug(
                    "startTransfer(): invoking edcConnectorClientApi.startTransferProcess() for {}",
                    edcTransferRequest);
            EdcAcknowledgementId acknowledgementId = edcConnectorTransferClient.startTransferProcess(
                    edcConsumerManagementUrl, createHttpHeadersMap(), edcTransferRequest);

            handleDeprovisioning(transferRequest, acknowledgementId);

            TransferProcessId transferProcessId = ModelUtil.toTransferProcessId(acknowledgementId);
            logTransferInitiated(transferProcessId, transferRequest);
            return transferProcessId;
        } catch (FeignException e) {
            log.error("startTransfer() failed", e);
            throw RemoteServiceUtil.toRemoteServiceErrorException(
                    EDCErrorType.REMOTE_EDC_CONNECTOR_ERROR, "startTransferProcess operation failed", e);
        }
    }

    /**
     * Best-effort emission of an enriched transfer business log (consumer, provider, asset) to be collected into
     * Elasticsearch and consumed by the Clearing House. Any failure resolving the enrichment data or emitting the
     * log is swallowed so it never breaks the transfer itself.
     */
    private void logTransferInitiated(TransferProcessId transferProcessId, TransferRequest request) {
        try {
            String transferId = transferProcessId == null ? null : transferProcessId.getTransferId();
            if (transferId == null) {
                log.warn("logTransferInitiated(): no transferProcessId returned, skipping enriched transfer log");
                return;
            }

            TransferRecordLog.TransferRecordLogBuilder record = TransferRecordLog.builder()
                    .transferProcessId(transferId)
                    .contractId(request.getContractId())
                    .providerEndpoint(request.getProviderEndpoint())
                    .consumerId(participantId);

            enrichFromTransferProcess(transferId, record);
            enrichFromContractAgreement(request.getContractId(), record);
            enrichFromPing(request.getProviderEndpoint(), record);
            enrichOwnConsumer(record);

            TransferRecordLog built = record.build();
            // cache the resolved identity so every subsequent state-change record carries the same uuids/names
            enrichmentByTransfer.put(transferId, new TransferEnrichment(
                    built.getProviderEndpoint(),
                    built.getProviderParticipantId(),
                    built.getProviderName(),
                    built.getConsumerParticipantId(),
                    built.getConsumerName()));
            emitTransferRecord(TransferBusinessOperation.TRANSFER_INITIATED, built);
        } catch (Exception e) {
            log.warn("logTransferInitiated(): could not emit enriched transfer log: {}", e.toString());
        }
    }

    /**
     * Best-effort emission of an enriched transfer business log on an observed state change (e.g. while polling the
     * transfer status). Reuses the already-resolved {@link TransferProcess} so no extra remote call is needed for
     * the transfer itself; the contract negotiation is resolved to enrich provider/agreement data.
     */
    protected void logTransferStateChange(TransferProcess transferProcess) {
        try {
            if (transferProcess == null || transferProcess.getTransferProcessId() == null) {
                return;
            }

            String transferId = transferProcess.getTransferProcessId();
            TransferRecordLog.TransferRecordLogBuilder record = TransferRecordLog.builder()
                    .transferProcessId(transferId)
                    .contractId(transferProcess.getContractId())
                    .assetId(transferProcess.getAssetId())
                    .transferType(transferProcess.getTransferType())
                    .state(transferProcess.getState())
                    .stateTimestamp(transferProcess.getStateTimestamp())
                    .consumerId(participantId);

            enrichFromContractAgreement(transferProcess.getContractId(), record);
            applyCachedEnrichment(transferId, record);

            if (transferProcess.getState() != null
                    && TERMINAL_STATES.contains(transferProcess.getState().toUpperCase())) {
                enrichmentByTransfer.remove(transferId);
            }
            emitTransferRecord(TransferBusinessOperation.TRANSFER_STATE_CHANGED, record.build());
        } catch (Exception e) {
            log.warn("logTransferStateChange(): could not emit enriched transfer log: {}", e.toString());
        }
    }

    /**
     * Applies the canonical participant identity resolved at initiation to a state-change record, so every record is
     * fully enriched without re-resolving. When it is not cached — e.g. the transfer was initiated before this
     * instance started — it falls back to resolving the consumer via {@code self} (the provider {@code ping} endpoint
     * is not available on the transfer process, so the provider identity then relies on the Clearing House retaining
     * it from the initiation record).
     *
     * <p>When the cached identity is only half-resolved (the authentication-provider was momentarily unavailable at
     * initiation) the missing side is resolved again here, so a transient failure does not blank the identity on every
     * record of the transfer. The retry is cheap: {@link ParticipantPingClient} caches the resolution.
     */
    private void applyCachedEnrichment(String transferId, TransferRecordLog.TransferRecordLogBuilder record) {
        TransferEnrichment cached = enrichmentByTransfer.get(transferId);
        if (cached == null) {
            enrichOwnConsumer(record);
            return;
        }
        if (cached.providerParticipantId() != null) {
            record.providerParticipantId(cached.providerParticipantId());
        }
        if (cached.providerName() != null) {
            record.providerName(cached.providerName());
        }
        if (cached.consumerParticipantId() != null) {
            record.consumerParticipantId(cached.consumerParticipantId());
        }
        if (cached.consumerName() != null) {
            record.consumerName(cached.consumerName());
        }
        // both helpers only overwrite what they can actually resolve, so the cached values are never lost
        if (!cached.isProviderResolved()) {
            enrichFromPing(cached.providerEndpoint(), record);
        }
        if (!cached.isConsumerResolved()) {
            enrichOwnConsumer(record);
        }
    }

    /**
     * Emits the enriched transfer record. The base implementation writes a BUSINESS-level technical log (collected
     * by Filebeat into {@code business-logs}) as a local audit trace. Kafka-enabled profiles override this to also
     * publish the record to a dedicated topic, which is the federation-ready ingestion path consumed by the
     * monitoring module and ultimately by the Clearing House.
     */
    protected void emitTransferRecord(TransferBusinessOperation businessOperation, TransferRecordLog record) {
        transferBusinessLogger.log(businessOperation, record);
    }

    private void enrichFromTransferProcess(String transferId, TransferRecordLog.TransferRecordLogBuilder record) {
        try {
            TransferProcess transferProcess = fetchTransferProcess(transferId);
            if (transferProcess != null) {
                record.assetId(transferProcess.getAssetId())
                        .transferType(transferProcess.getTransferType())
                        .state(transferProcess.getState())
                        .stateTimestamp(transferProcess.getStateTimestamp());
            }
        } catch (Exception e) {
            log.warn("enrichFromTransferProcess(): could not resolve transfer process {}: {}", transferId, e.toString());
        }
    }

    /**
     * Resolves provider and consumer identities from the EDC contract agreement. The transfer's {@code contractId}
     * is the contract <em>agreement</em> id, and the agreement carries the authoritative participant identities as
     * known by the EDC connector. The provider/consumer ids resolved here take precedence over any locally
     * configured {@code participant.id} (which remains as a fallback for the consumer side).
     */
    /**
     * Resolves the provider's verified identity ({@code id} + {@code organization}) from the authentication-provider
     * {@code ping} endpoint, using the connection endpoint FQDN. The ping id (the canonical participant id) is stored
     * as a separate {@code providerParticipantId} attribute — kept alongside the agreement's {@code providerId} for
     * compatibility and full traceability — and the organization is set as the provider name. Best-effort: on any
     * failure the values resolved earlier are kept.
     */
    /**
     * Resolves this connector's own verified identity from the authentication-provider and stores it as the
     * consumer's canonical {@code consumerParticipantId} + {@code consumerName}, kept alongside the configured
     * {@code consumerId} for compatibility and full traceability. Best-effort.
     */
    private void enrichOwnConsumer(TransferRecordLog.TransferRecordLogBuilder record) {
        try {
            ParticipantPingClient.ParticipantIdentity own = participantPingClient.resolveOwn();
            if (own != null) {
                if (own.id() != null) {
                    record.consumerParticipantId(own.id());
                }
                if (own.organization() != null) {
                    record.consumerName(own.organization());
                }
            }
        } catch (Exception e) {
            log.warn("enrichOwnConsumer(): could not resolve own identity: {}", e.toString());
        }
    }

    private void enrichFromPing(String connectionUrl, TransferRecordLog.TransferRecordLogBuilder record) {
        try {
            ParticipantPingClient.ParticipantIdentity identity =
                    participantPingClient.resolveByEndpoint(connectionUrl);
            if (identity != null) {
                if (identity.id() != null) {
                    record.providerParticipantId(identity.id());
                }
                if (identity.organization() != null) {
                    record.providerName(identity.organization());
                }
            }
        } catch (Exception e) {
            log.warn("enrichFromPing(): could not resolve provider identity for {}: {}", connectionUrl, e.toString());
        }
    }

    private void enrichFromContractAgreement(String agreementId, TransferRecordLog.TransferRecordLogBuilder record) {
        if (agreementId == null) {
            return;
        }
        try {
            EdcContractAgreement agreement = contractNegotiationService.getContractAgreement(agreementId);
            if (agreement != null) {
                record.contractAgreementId(
                        agreement.getContractAgreementId() != null ? agreement.getContractAgreementId() : agreementId);
                if (agreement.getProviderId() != null && !agreement.getProviderId().isBlank()) {
                    record.providerId(agreement.getProviderId());
                }
                if (agreement.getConsumerId() != null && !agreement.getConsumerId().isBlank()) {
                    record.consumerId(agreement.getConsumerId());
                }
            }
        } catch (Exception e) {
            log.warn(
                    "enrichFromContractAgreement(): could not resolve agreement {}: {}", agreementId, e.toString());
        }
    }

    /**
     * removes the deprovisioningEnabled property from the dataDestination as this
     * is not expected by EDC Connector
     * 
     * @param dataDestination
     * @return a copy of the dataDestination without the deprovisioningEnabled
     *         property if it was present, otherwise the original dataDestination
     */
    private JsonNode normalizeDataDestination(JsonNode dataDestination) {
        if (!dataDestination.has(DEPROVISIONING_ENABLED_PROPERTY_NAME)) {
            return dataDestination;
        }
        ObjectNode copy = dataDestination.deepCopy();
        copy.remove(DEPROVISIONING_ENABLED_PROPERTY_NAME);
        return copy;
    }

    protected void handleDeprovisioning(TransferRequest transferRequest, EdcAcknowledgementId acknowledgementId) {
        log.info(
                "handleDeprovisioning() ignored by '{}' implementation",
                this.getClass().getName());
    }

    protected boolean isDeprovisioningEnabled(TransferRequest transferRequest) {
        JsonNode dataDestination = transferRequest.getDataDestination();
        return dataDestination.has(DEPROVISIONING_ENABLED_PROPERTY_NAME)
                && dataDestination.get(DEPROVISIONING_ENABLED_PROPERTY_NAME).asBoolean();
    }

    @Override
    public TransferProcess getTransferProcess(String transferProcessId) {
        TransferProcess transferProcess = fetchTransferProcess(transferProcessId);
        // the consumer polls this endpoint while the transfer progresses; emit an enriched record on each observed
        // state so the Clearing House can track the state changes and expose the latest state
        logTransferStateChange(transferProcess);
        return transferProcess;
    }

    protected TransferProcess fetchTransferProcess(String transferProcessId) {
        EdcTransferProcess edcTransferProcess;
        try {
            log.debug(
                    "getTransferProcess(): invoking edcConnectorClientApi.getTransferProcess() for transferProcessId {}",
                    transferProcessId);
            edcTransferProcess = edcConnectorTransferClient.getTransferProcess(
                    edcConsumerManagementUrl, createHttpHeadersMap(), transferProcessId);
        } catch (FeignException e) {
            log.error("getTransferProcess() failed", e);
            throw RemoteServiceUtil.toRemoteServiceErrorException(
                    EDCErrorType.REMOTE_EDC_CONNECTOR_ERROR, "getTransferProcess operation failed", e);
        }

        String state = edcTransferProcess.getState();

        // this is missing in the kafka impl
        edcTransferProcess.setFinalState(state);

        return ModelUtil.transform(edcTransferProcess);
    }

    private String getTransferType(TransferRequest transferRequest) {
        if (transferRequest.getDataDestination().get("type").textValue().endsWith("-PULL")) {
            return transferRequest.getDataDestination().get("type").textValue();
        }
        // in the dataDestination template validated by validation service this field is
        // mandatory
        return transferRequest.getDataDestination().get("type").textValue() + "-PUSH";
    }

    protected Map<String, String> createHttpHeadersMap() {
        Map<String, String> headers = new HashMap<>();
        headers.put(Constants.EDC_API_KEY_HEADER, edcConnectorApiKeyValue);
        headers.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        return headers;
    }
}
