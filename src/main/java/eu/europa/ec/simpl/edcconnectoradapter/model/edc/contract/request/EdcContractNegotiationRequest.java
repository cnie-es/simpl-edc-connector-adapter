package eu.europa.ec.simpl.edcconnectoradapter.model.edc.contract.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import eu.europa.ec.simpl.edcconnectoradapter.constant.Constants;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class EdcContractNegotiationRequest {

    @JsonProperty("@context")
    @Builder.Default
    private Map<String, String> context =
            Map.of("@vocab", Constants.EDC_PREFIX, "odrl", "http://www.w3.org/ns/odrl/2/");

    @JsonProperty("@type")
    @Builder.Default
    private String type = "ContractRequest";

    private String counterPartyAddress;

    @Builder.Default
    private String protocol = "dataspace-protocol-http";

    private Object policy;
}
