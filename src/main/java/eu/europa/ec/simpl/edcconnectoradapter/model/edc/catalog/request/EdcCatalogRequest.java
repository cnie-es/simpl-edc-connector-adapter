package eu.europa.ec.simpl.edcconnectoradapter.model.edc.catalog.request;

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
public class EdcCatalogRequest {

    @JsonProperty("@context")
    @Builder.Default
    private Map<String, String> context = Map.of("@vocab", Constants.EDC_PREFIX);

    @JsonProperty("@type")
    @Builder.Default
    private String type = "CatalogRequest";

    private String counterPartyAddress;

    @Builder.Default
    private String protocol = "dataspace-protocol-http";

    private EdcQueryPayload querySpec;
}
