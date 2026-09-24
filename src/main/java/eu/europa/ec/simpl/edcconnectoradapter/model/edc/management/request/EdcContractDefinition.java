package eu.europa.ec.simpl.edcconnectoradapter.model.edc.management.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class EdcContractDefinition extends EdcAbstractManagement {

    private String accessPolicyId;

    private String contractPolicyId;

    @JsonProperty("assetsSelector")
    private List<EdcAssetsSelector> assetsSelectors = new ArrayList<>();

    public EdcContractDefinition(String assetId, String accessPolicyId, String contractPolicyId) {
        this.accessPolicyId = accessPolicyId;
        this.contractPolicyId = contractPolicyId;
        this.assetsSelectors.add(new EdcAssetsSelector(assetId));
    }
}
