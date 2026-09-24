package eu.europa.ec.simpl.edcconnectoradapter.model.edc.contract.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class EdcContractNegotiation {

    @JsonProperty("@id")
    private String contractNegotiationId;
    // is null until state == FINALIZED
    private String contractAgreementId;

    private String state;

    private String counterPartyAddress;

    private String counterPartyId;

    private String errorDetail;

    private String protocol;

    private String type;

    private long createdAt;
}
