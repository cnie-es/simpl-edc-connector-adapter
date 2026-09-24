package eu.europa.ec.simpl.edcconnectoradapter.model.edc.transfer;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Enriched view of a transfer process meant to be emitted as a structured business log (and collected into
 * Elasticsearch). It joins data available on the {@code TransferProcess} with data resolved from its
 * {@code ContractNegotiation}, plus the own consumer identity, so the Clearing House can reconstruct who was
 * involved (consumer, provider) and what asset was transferred.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransferRecordLog {

    private String transferProcessId;

    private String contractId;

    private String contractAgreementId;

    private String assetId;

    private String providerId;

    private String providerParticipantId;

    private String providerName;

    private String providerEndpoint;

    private String consumerId;

    private String consumerParticipantId;

    private String consumerName;

    private String state;

    private String transferType;

    private Long createdAt;

    private Long stateTimestamp;

    /**
     * Flattens the record into non-null string fields, suitable for the {@code customFields} of a structured log
     * message so that each value becomes an independently queryable field in Elasticsearch.
     */
    public Map<String, String> toCustomFields() {
        Map<String, String> fields = new LinkedHashMap<>();
        putIfPresent(fields, "transferProcessId", transferProcessId);
        putIfPresent(fields, "contractId", contractId);
        putIfPresent(fields, "contractAgreementId", contractAgreementId);
        putIfPresent(fields, "assetId", assetId);
        putIfPresent(fields, "providerId", providerId);
        putIfPresent(fields, "providerParticipantId", providerParticipantId);
        putIfPresent(fields, "providerName", providerName);
        putIfPresent(fields, "providerEndpoint", providerEndpoint);
        putIfPresent(fields, "consumerId", consumerId);
        putIfPresent(fields, "consumerParticipantId", consumerParticipantId);
        putIfPresent(fields, "consumerName", consumerName);
        putIfPresent(fields, "state", state);
        putIfPresent(fields, "transferType", transferType);
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
