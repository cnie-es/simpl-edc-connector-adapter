package eu.europa.ec.simpl.edcconnectoradapter.client.edcconnector;

import eu.europa.ec.simpl.edcconnectoradapter.model.edc.management.request.EdcAssetDefinition;
import eu.europa.ec.simpl.edcconnectoradapter.model.edc.management.request.EdcContractDefinition;
import eu.europa.ec.simpl.edcconnectoradapter.model.edc.management.request.EdcPolicyDefinition;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(value = "edcConnectorClient", url = "${edc-connector.base-url}")
public interface EDCConnectorManagementClient {

    String EDC_MANAGEMENT_PATH = "/management/v3";

    @PostMapping(value = EDC_MANAGEMENT_PATH + "/assets", consumes = "application/json", produces = "application/json")
    ResponseEntity<String> registerAsset(@RequestBody String body);

    @PostMapping(value = EDC_MANAGEMENT_PATH + "/assets", consumes = "application/json", produces = "application/json")
    ResponseEntity<String> register(@RequestBody EdcAssetDefinition asset);

    @PostMapping(
            value = EDC_MANAGEMENT_PATH + "/policydefinitions",
            consumes = "application/json",
            produces = "application/json")
    ResponseEntity<String> registerPolicyDefinition(@RequestBody String body);

    @PostMapping(
            value = EDC_MANAGEMENT_PATH + "/policydefinitions",
            consumes = "application/json",
            produces = "application/json")
    ResponseEntity<String> register(@RequestBody EdcPolicyDefinition policyDefinition);

    @PostMapping(
            value = EDC_MANAGEMENT_PATH + "/contractdefinitions",
            consumes = "application/json",
            produces = "application/json")
    ResponseEntity<String> registerContractDefinition(@RequestBody String body);

    @PostMapping(
            value = EDC_MANAGEMENT_PATH + "/contractdefinitions",
            consumes = "application/json",
            produces = "application/json")
    ResponseEntity<String> register(@RequestBody EdcContractDefinition contractDefinition);
}
