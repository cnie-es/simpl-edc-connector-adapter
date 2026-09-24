package eu.europa.ec.simpl.edcconnectoradapter.service.consumer.contractnegotiation;

import com.fasterxml.jackson.databind.JsonNode;
import eu.europa.ec.simpl.data1.common.adapter.connector.model.catalog.CatalogSearchResult;
import eu.europa.ec.simpl.data1.common.adapter.connector.model.contract.ContractNegotiation;
import eu.europa.ec.simpl.data1.common.adapter.connector.model.contract.ContractNegotiationId;
import eu.europa.ec.simpl.data1.common.adapter.connector.model.contract.ContractNegotiationRequest;
import eu.europa.ec.simpl.data1.common.util.RemoteServiceUtil;
import eu.europa.ec.simpl.edcconnectoradapter.client.authprovider.ParticipantPingClient;
import eu.europa.ec.simpl.edcconnectoradapter.client.edcconnector.EDCConnectorCatalogClient;
import eu.europa.ec.simpl.edcconnectoradapter.client.edcconnector.EDCConnectorContractClient;
import eu.europa.ec.simpl.edcconnectoradapter.constant.ContractBusinessOperation;
import eu.europa.ec.simpl.edcconnectoradapter.constant.Constants;
import eu.europa.ec.simpl.edcconnectoradapter.enumeration.EDCErrorType;
import eu.europa.ec.simpl.edcconnectoradapter.logging.ContractRecordEmitter;
import eu.europa.ec.simpl.edcconnectoradapter.model.edc.catalog.request.EdcCatalogRequest;
import eu.europa.ec.simpl.edcconnectoradapter.model.edc.contract.ContractRecordLog;
import eu.europa.ec.simpl.edcconnectoradapter.model.edc.catalog.request.EdcCriterion;
import eu.europa.ec.simpl.edcconnectoradapter.model.edc.catalog.request.EdcQueryPayload;
import eu.europa.ec.simpl.edcconnectoradapter.model.edc.common.response.EdcAcknowledgementId;
import eu.europa.ec.simpl.edcconnectoradapter.model.edc.contract.request.EdcContractNegotiationRequest;
import eu.europa.ec.simpl.edcconnectoradapter.model.edc.contract.response.EdcContractAgreement;
import eu.europa.ec.simpl.edcconnectoradapter.model.edc.contract.response.EdcContractNegotiation;
import eu.europa.ec.simpl.edcconnectoradapter.service.catalogmapper.CatalogMapperService;
import eu.europa.ec.simpl.edcconnectoradapter.util.ModelUtil;
import eu.europa.ec.simpl.edcconnectoradapter.util.PathUtil;
import feign.FeignException;
import jakarta.annotation.PostConstruct;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

@Profile({"consumer", "build", "test"})
@Log4j2
@Service
@RequiredArgsConstructor
public class ContractNegotiationServiceImpl implements ContractNegotiationService {

    @Value("${edc-connector.base-url}")
    private String edcConnectorBaseUrl;

    @Value("${edc-connector.api-key.value}")
    private String edcConnectorApiKeyValue;

    // own participant identity (consumer side), kept as a fallback consumerId for the enriched contract record
    @Value("${edc-connector.participant.id:}")
    private String participantId;

    private final EDCConnectorCatalogClient edcConnectorCatalogClient;
    private final EDCConnectorContractClient edcConnectorContractClient;
    private final CatalogMapperService catalogMapperService;
    private final ParticipantPingClient participantPingClient;
    private final ContractRecordEmitter contractRecordEmitter;

    // EDC negotiation states that close the negotiation; the cached enrichment is evicted once one is reached
    private static final Set<String> TERMINAL_STATES = Set.of("FINALIZED", "TERMINATED");

    // canonical participant identity (and asset) resolved once at initiation, reused on every state-change emission so
    // each emitted record carries the uuids/names even when they cannot be re-resolved at poll time
    private final Map<String, ContractEnrichment> enrichmentByNegotiation = new ConcurrentHashMap<>();

    private record ContractEnrichment(
            String providerParticipantId,
            String providerName,
            String consumerParticipantId,
            String consumerName,
            String assetId) {

        boolean isProviderResolved() {
            return providerParticipantId != null && providerName != null;
        }

        boolean isConsumerResolved() {
            return consumerParticipantId != null && consumerName != null;
        }
    }

    protected URI edcConsumerManagementUrl;

    @PostConstruct
    public void init() {
        edcConsumerManagementUrl = URI.create(PathUtil.checkSuffix(Constants.EDC_MANAGEMENT_PATH, edcConnectorBaseUrl));
        log.info("init(): edcConsumerManagementUrl='{}'", edcConsumerManagementUrl);
    }

    @Override
    public CatalogSearchResult getCatalog(ContractNegotiationRequest request) {
        JsonNode edcResponse = requestCatalog(request);
        return catalogMapperService.mapEdcCatalog(edcResponse, request.getContractDefinitionId());
    }

    private JsonNode requestCatalog(ContractNegotiationRequest request) {
        log.debug("requestCatalog() for {}", request);

        String assetId = request.getAssetId();

        List<EdcCriterion> criteria = new ArrayList<>();
        criteria.add(EdcCriterion.builder()
                .operandLeft(Constants.EDC_PREFIX + Constants.EDC_ASSET_ID_PROPERTY)
                .operator(Constants.EDC_EQUAL)
                .operandRight(assetId)
                .build());

        EdcQueryPayload edcQueryPayload = EdcQueryPayload.builder()
                .offset(0)
                .limit(Constants.DEFAULT_EDC_QUERY_LIMIT)
                .filterExpression(criteria)
                .build();

        String providerProtocolUrl = PathUtil.checkSuffix(Constants.EDC_PROTOCOL_PATH, request.getProviderEndpoint());

        EdcCatalogRequest edcCatalogRequest = EdcCatalogRequest.builder()
                .counterPartyAddress(providerProtocolUrl)
                .querySpec(edcQueryPayload)
                .build();
        try {
            log.debug(
                    "requestCatalog(): invoking edcConnectorCatalogClient.requestCatalog() for {}", edcCatalogRequest);
            return edcConnectorCatalogClient.requestCatalog(edcConsumerManagementUrl, getHeaders(), edcCatalogRequest);
        } catch (FeignException e) {
            log.error("requestCatalog() failed", e);
            throw RemoteServiceUtil.toRemoteServiceErrorException(
                    EDCErrorType.REMOTE_EDC_CONNECTOR_ERROR, "requestCatalog operation failed", e);
        }
    }

    @Override
    public ContractNegotiationId initiateContractNegotiation(ContractNegotiationRequest request) {
        log.debug("initiateContractNegotiation() for {}", request);
        JsonNode providerCatalog = requestCatalog(request);
        JsonNode offerForNegotiation = catalogMapperService.mapEdcCatalogOfferForNegotiation(
                providerCatalog, request.getContractDefinitionId());

        String providerProtocolUrl = PathUtil.checkSuffix(Constants.EDC_PROTOCOL_PATH, request.getProviderEndpoint());

        EdcContractNegotiationRequest edcContractNegotiationRequest = EdcContractNegotiationRequest.builder()
                .counterPartyAddress(providerProtocolUrl)
                .policy(offerForNegotiation)
                .build();

        try {
            log.debug(
                    "initiateContractNegotiation(): invoking edcConnectorContractClient.initiateContractNegotiation() for {}",
                    edcContractNegotiationRequest);
            EdcAcknowledgementId acknowledgementId = edcConnectorContractClient.initiateContractNegotiation(
                    edcConsumerManagementUrl, getHeaders(), edcContractNegotiationRequest);
            ContractNegotiationId negotiationId = ModelUtil.toContractNegotiationId(acknowledgementId);
            logContractNegotiationInitiated(negotiationId, request);
            return negotiationId;
        } catch (FeignException e) {
            log.error("initiateContractNegotiation() failed", e);
            throw RemoteServiceUtil.toRemoteServiceErrorException(
                    EDCErrorType.REMOTE_EDC_CONNECTOR_ERROR, "initiateContractNegotiation operation failed", e);
        }
    }

    /**
     * Best-effort emission of an enriched contract-negotiation record (consumer, provider, asset/offer) to be
     * collected into Elasticsearch and consumed by the Clearing House, mirroring {@code logTransferInitiated} on the
     * transfer flow. The provider identity is resolved from the {@code ping} endpoint (by the provider connection
     * endpoint), the consumer identity from {@code self}. Any failure resolving the enrichment data or emitting the
     * record is swallowed so it never breaks the negotiation itself.
     */
    private void logContractNegotiationInitiated(ContractNegotiationId negotiationId, ContractNegotiationRequest request) {
        try {
            String id = negotiationId == null ? null : negotiationId.getNegotiationId();
            if (id == null) {
                log.warn("logContractNegotiationInitiated(): no negotiation id returned, skipping enriched record");
                return;
            }

            long now = System.currentTimeMillis();
            ContractRecordLog.ContractRecordLogBuilder record = ContractRecordLog.builder()
                    .contractNegotiationId(id)
                    .contractDefinitionId(request.getContractDefinitionId())
                    .assetId(request.getAssetId())
                    .providerEndpoint(request.getProviderEndpoint())
                    .consumerId(participantId)
                    // observation time; the real state/createdAt are resolved from the negotiation below (no fabricated state)
                    .createdAt(now)
                    .stateTimestamp(now);

            enrichFromNegotiation(id, record);
            enrichProviderFromPing(request.getProviderEndpoint(), record);
            enrichOwnConsumer(record);

            ContractRecordLog built = record.build();
            // cache the resolved identity so every subsequent state-change record carries the same uuids/names/asset
            enrichmentByNegotiation.put(id, new ContractEnrichment(
                    built.getProviderParticipantId(),
                    built.getProviderName(),
                    built.getConsumerParticipantId(),
                    built.getConsumerName(),
                    built.getAssetId()));
            contractRecordEmitter.emit(ContractBusinessOperation.CONTRACT_NEGOTIATION_INITIATED, built);
        } catch (Exception e) {
            log.warn("logContractNegotiationInitiated(): could not emit enriched contract record: {}", e.toString());
        }
    }

    /**
     * Resolves the negotiation's current state, creation time and (once available) agreement id from the EDC
     * connector. Best-effort: on any failure the values already set on the record are kept.
     */
    private void enrichFromNegotiation(String negotiationId, ContractRecordLog.ContractRecordLogBuilder record) {
        try {
            applyNegotiationState(fetchContractNegotiation(negotiationId), record);
        } catch (Exception e) {
            log.warn("enrichFromNegotiation(): could not resolve negotiation {}: {}", negotiationId, e.toString());
        }
    }

    /**
     * Applies the negotiation's real state, creation time and agreement id onto the record. The transient
     * {@code INITIAL} state is intentionally not recorded (it is not a meaningful negotiation state for the Clearing
     * House); the first meaningful state observed via polling is what surfaces.
     */
    private void applyNegotiationState(ContractNegotiation negotiation, ContractRecordLog.ContractRecordLogBuilder record) {
        if (negotiation == null) {
            return;
        }
        String state = negotiation.getState();
        if (state != null && !state.isBlank() && !"INITIAL".equalsIgnoreCase(state)) {
            record.state(state);
        }
        if (negotiation.getCreatedAt() > 0) {
            record.createdAt(negotiation.getCreatedAt());
        }
        if (negotiation.getContractAgreementId() != null && !negotiation.getContractAgreementId().isBlank()) {
            record.contractAgreementId(negotiation.getContractAgreementId());
        }
    }

    /**
     * Resolves the provider's verified identity ({@code id} + {@code organization}) from the authentication-provider
     * {@code ping} endpoint, using the provider connection endpoint FQDN, and stores it as the canonical
     * {@code providerParticipantId} + {@code providerName}. Best-effort.
     */
    private void enrichProviderFromPing(String connectionUrl, ContractRecordLog.ContractRecordLogBuilder record) {
        try {
            ParticipantPingClient.ParticipantIdentity identity = participantPingClient.resolveByEndpoint(connectionUrl);
            if (identity != null) {
                if (identity.id() != null) {
                    record.providerParticipantId(identity.id());
                }
                if (identity.organization() != null) {
                    record.providerName(identity.organization());
                }
            }
        } catch (Exception e) {
            log.warn(
                    "enrichProviderFromPing(): could not resolve provider identity for {}: {}",
                    connectionUrl,
                    e.toString());
        }
    }

    /**
     * Resolves this connector's own verified identity from the authentication-provider and stores it as the
     * consumer's canonical {@code consumerParticipantId} + {@code consumerName}, kept alongside the configured
     * {@code consumerId} for compatibility and full traceability. Best-effort.
     */
    private void enrichOwnConsumer(ContractRecordLog.ContractRecordLogBuilder record) {
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

    @Override
    public ContractNegotiation getContractNegotiation(String negotiationId) {
        ContractNegotiation negotiation = fetchContractNegotiation(negotiationId);
        // the consumer polls this endpoint while the negotiation progresses; emit an enriched record on each observed
        // state so the Clearing House can track the state changes and expose the latest state
        logContractStateChange(negotiation);
        return negotiation;
    }

    private ContractNegotiation fetchContractNegotiation(String negotiationId) {
        try {
            log.debug(
                    "getContractNegotiation(): invoking edcConnectorContractClient.getContractNegotiation() for negotiationId {}",
                    negotiationId);
            EdcContractNegotiation edcContractNegotiation = edcConnectorContractClient.getContractNegotiation(
                    edcConsumerManagementUrl, getHeaders(), negotiationId);
            return ModelUtil.transform(edcContractNegotiation);
        } catch (FeignException e) {
            log.error("getContractNegotiation() failed", e);
            throw RemoteServiceUtil.toRemoteServiceErrorException(
                    EDCErrorType.REMOTE_EDC_CONNECTOR_ERROR, "getContractNegotiation operation failed", e);
        }
    }

    /**
     * Best-effort emission of an enriched contract-negotiation record for an observed negotiation state, so the
     * Clearing House accumulates the state changes (and exposes the latest state). The provider connection endpoint is
     * taken from the negotiation's counter-party address (for the {@code ping}); the consumer identity from
     * {@code self}. The asset id is not carried here (it is supplied by the initiation record and retained downstream).
     */
    private void logContractStateChange(ContractNegotiation negotiation) {
        try {
            if (negotiation == null || negotiation.getContractNegotiationId() == null) {
                return;
            }
            String id = negotiation.getContractNegotiationId();
            ContractRecordLog.ContractRecordLogBuilder record = ContractRecordLog.builder()
                    .contractNegotiationId(id)
                    .providerEndpoint(negotiation.getCounterPartyAddress())
                    .consumerId(participantId)
                    .stateTimestamp(System.currentTimeMillis());
            applyNegotiationState(negotiation, record);
            applyEnrichment(id, negotiation.getCounterPartyAddress(), record);

            if (negotiation.getState() != null && TERMINAL_STATES.contains(negotiation.getState().toUpperCase())) {
                enrichmentByNegotiation.remove(id);
            }
            contractRecordEmitter.emit(ContractBusinessOperation.CONTRACT_STATE_CHANGED, record.build());
        } catch (Exception e) {
            log.warn("logContractStateChange(): could not emit enriched contract record: {}", e.toString());
        }
    }

    /**
     * Applies the canonical participant identity (and asset) to a state-change record. Reuses the identity resolved at
     * initiation (so every record is fully enriched without re-resolving); when it is not cached — e.g. the negotiation
     * was initiated before this instance started — it falls back to resolving the provider via {@code ping} and the
     * consumer via {@code self}.
     *
     * <p>When the cached identity is only half-resolved (the authentication-provider was momentarily unavailable at
     * initiation) the missing side is resolved again here, so a transient failure does not blank the identity on every
     * record of the negotiation. The retry is cheap: {@link ParticipantPingClient} caches the resolution.
     */
    private void applyEnrichment(String negotiationId, String providerEndpoint, ContractRecordLog.ContractRecordLogBuilder record) {
        ContractEnrichment cached = enrichmentByNegotiation.get(negotiationId);
        if (cached == null) {
            enrichProviderFromPing(providerEndpoint, record);
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
        if (cached.assetId() != null) {
            record.assetId(cached.assetId());
        }
        // both helpers only overwrite what they can actually resolve, so the cached values are never lost
        if (!cached.isProviderResolved()) {
            enrichProviderFromPing(providerEndpoint, record);
        }
        if (!cached.isConsumerResolved()) {
            enrichOwnConsumer(record);
        }
    }

    @Override
    public EdcContractAgreement getContractAgreement(String agreementId) {
        try {
            log.debug(
                    "getContractAgreement(): invoking edcConnectorContractClient.getContractAgreement() for agreementId {}",
                    agreementId);
            return edcConnectorContractClient.getContractAgreement(edcConsumerManagementUrl, getHeaders(), agreementId);
        } catch (FeignException e) {
            log.error("getContractAgreement() failed", e);
            throw RemoteServiceUtil.toRemoteServiceErrorException(
                    EDCErrorType.REMOTE_EDC_CONNECTOR_ERROR, "getContractAgreement operation failed", e);
        }
    }

    protected Map<String, String> getHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put(Constants.EDC_API_KEY_HEADER, edcConnectorApiKeyValue);
        headers.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        return headers;
    }
}
