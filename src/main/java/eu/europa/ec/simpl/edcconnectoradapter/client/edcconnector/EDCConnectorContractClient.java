package eu.europa.ec.simpl.edcconnectoradapter.client.edcconnector;

import eu.europa.ec.simpl.edcconnectoradapter.model.edc.common.response.EdcAcknowledgementId;
import eu.europa.ec.simpl.edcconnectoradapter.model.edc.contract.response.EdcContractAgreement;
import eu.europa.ec.simpl.edcconnectoradapter.model.edc.contract.response.EdcContractNegotiation;
import java.net.URI;
import java.util.Map;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(value = "edcConnectorContractClient", url = "placeholder")
public interface EDCConnectorContractClient {

    @PostMapping("/v3/contractnegotiations")
    EdcAcknowledgementId initiateContractNegotiation(
            URI hostname, @RequestHeader Map<String, String> header, @RequestBody Object requestBody);

    @GetMapping("/v3/contractnegotiations/{id}")
    EdcContractNegotiation getContractNegotiation(
            URI hostname, @RequestHeader Map<String, String> header, @PathVariable String id);

    @GetMapping("/v3/contractagreements/{id}")
    EdcContractAgreement getContractAgreement(
            URI hostname, @RequestHeader Map<String, String> header, @PathVariable String id);
}
