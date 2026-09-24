package eu.europa.ec.simpl.edcconnectoradapter.model.edc.catalog.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EdcCriterion {

    @JsonProperty("@type")
    @Builder.Default
    private String type = "Criterion";

    private Object operandLeft;
    private String operator;
    private Object operandRight;
}
