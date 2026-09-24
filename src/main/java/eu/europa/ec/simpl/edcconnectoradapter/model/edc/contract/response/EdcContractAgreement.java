package eu.europa.ec.simpl.edcconnectoradapter.model.edc.contract.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * EDC contract agreement as returned by {@code GET /v3/contractagreements/{id}}. It carries the authoritative
 * participant identities (provider and consumer) as known by the EDC connector, which is preferred over any locally
 * configured identifier when enriching transfer records.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EdcContractAgreement {

    @JsonProperty("@id")
    private String contractAgreementId;

    private String providerId;

    private String consumerId;

    private String assetId;
}
