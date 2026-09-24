package eu.europa.ec.simpl.edcconnectoradapter.model.edc.catalog.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import eu.europa.ec.simpl.edcconnectoradapter.constant.Constants;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class EdcQueryPayload {

    @JsonProperty("@context")
    @Builder.Default
    private Map<String, String> context = Map.of("edc", Constants.EDC_PREFIX);

    @JsonProperty("@type")
    @Builder.Default
    private String type = "QuerySpec";

    private int offset;

    @Builder.Default
    private int limit = Constants.DEFAULT_EDC_QUERY_LIMIT;

    private String sortOrder;

    private String sortField;

    private List<EdcCriterion> filterExpression;
}
