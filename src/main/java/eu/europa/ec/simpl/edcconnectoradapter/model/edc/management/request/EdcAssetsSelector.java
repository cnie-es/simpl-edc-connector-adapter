package eu.europa.ec.simpl.edcconnectoradapter.model.edc.management.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class EdcAssetsSelector {

    @JsonProperty("@type")
    private String type = "CriterionDto";

    private String operandLeft = "https://w3id.org/edc/v0.0.1/ns/id";

    private String operator = "=";

    private String operandRight;

    public EdcAssetsSelector(String id) {
        this.operandRight = id;
    }
}
