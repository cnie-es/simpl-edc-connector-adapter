package eu.europa.ec.simpl.edcconnectoradapter.client.edcconnector;

import eu.europa.ec.simpl.edcconnectoradapter.model.edc.common.response.EdcAcknowledgementId;
import eu.europa.ec.simpl.edcconnectoradapter.model.edc.transfer.response.EdcTransferProcess;
import java.net.URI;
import java.util.Map;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(value = "edcConnectorTransferClient", url = "placeholder")
public interface EDCConnectorTransferClient {

    @PostMapping("/v3/transferprocesses")
    EdcAcknowledgementId startTransferProcess(
            URI hostname, @RequestHeader Map<String, String> header, @RequestBody Object requestBody);

    @GetMapping("/v3/transferprocesses/{id}")
    EdcTransferProcess getTransferProcess(
            URI hostname, @RequestHeader Map<String, String> header, @PathVariable String id);

    @PostMapping("/v3/transferprocesses/{id}/deprovision")
    Object deprovisioningTransferProcess(
            URI hostname, @RequestHeader Map<String, String> header, @PathVariable String id);
}
