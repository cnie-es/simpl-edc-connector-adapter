package eu.europa.ec.simpl.edcconnectoradapter.client.edcconnector;

import com.fasterxml.jackson.databind.JsonNode;
import java.net.URI;
import java.util.Map;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(value = "edcConnectorCatalogClient", url = "placeholder")
public interface EDCConnectorCatalogClient {

    @PostMapping("/v3/catalog/request")
    JsonNode requestCatalog(URI hostname, @RequestHeader Map<String, String> header, @RequestBody Object requestBody);
}
