package eu.europa.ec.simpl.edcconnectoradapter.model.edc.transfer.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
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
public class EdcTransferRequest {

    @JsonProperty("@context")
    @Builder.Default
    private Map<String, String> context = Map.of("@edc", Constants.EDC_PREFIX);

    @JsonProperty("@type")
    @Builder.Default
    private String type = "TransferRequestDto";

    @Builder.Default
    private String protocol = "dataspace-protocol-http";

    private String counterPartyAddress;

    private String contractId;

    private String transferType;

    private JsonNode dataDestination;
}
