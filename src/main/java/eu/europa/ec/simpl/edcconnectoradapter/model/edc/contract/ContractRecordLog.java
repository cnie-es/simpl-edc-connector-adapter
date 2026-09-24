package eu.europa.ec.simpl.edcconnectoradapter.model.edc.contract;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Enriched view of a contract negotiation meant to be emitted as a structured business log (and collected into
 * Elasticsearch, via a dedicated Kafka topic, into {@code contract-records-*}). It joins the data available when the
 * consumer initiates the negotiation through the adapter with the verified provider/consumer identities resolved from
 * the authentication-provider ({@code ping} for the provider by its connection endpoint, {@code self} for the
 * consumer), so the Clearing House can reconstruct who was involved and over which asset/offer, keyed by the
 * negotiation id. This mirrors {@code TransferRecordLog} for the transfer flow.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ContractRecordLog {

    private String contractNegotiationId;

    private String contractAgreementId;

    private String contractDefinitionId;

    private String offerId;

    private String assetId;

    private String providerId;

    private String providerParticipantId;

    private String providerName;

    private String providerEndpoint;

    private String consumerId;

    private String consumerParticipantId;

    private String consumerName;

    private String state;

    private Long createdAt;

    private Long stateTimestamp;

    /**
     * Flattens the record into non-null string fields, suitable for the {@code customFields} of a structured log
     * message so that each value becomes an independently queryable field in Elasticsearch.
     */
    public Map<String, String> toCustomFields() {
        Map<String, String> fields = new LinkedHashMap<>();
        putIfPresent(fields, "contractNegotiationId", contractNegotiationId);
        putIfPresent(fields, "contractAgreementId", contractAgreementId);
        putIfPresent(fields, "contractDefinitionId", contractDefinitionId);
        putIfPresent(fields, "offerId", offerId);
        putIfPresent(fields, "assetId", assetId);
        putIfPresent(fields, "providerId", providerId);
        putIfPresent(fields, "providerParticipantId", providerParticipantId);
        putIfPresent(fields, "providerName", providerName);
        putIfPresent(fields, "providerEndpoint", providerEndpoint);
        putIfPresent(fields, "consumerId", consumerId);
        putIfPresent(fields, "consumerParticipantId", consumerParticipantId);
        putIfPresent(fields, "consumerName", consumerName);
        putIfPresent(fields, "state", state);
        putIfPresent(fields, "createdAt", createdAt == null ? null : String.valueOf(createdAt));
        putIfPresent(fields, "stateTimestamp", stateTimestamp == null ? null : String.valueOf(stateTimestamp));
        return fields;
    }

    private static void putIfPresent(Map<String, String> fields, String key, String value) {
        if (value != null && !value.isBlank()) {
            fields.put(key, value);
        }
    }
}
