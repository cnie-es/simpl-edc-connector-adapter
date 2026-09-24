package eu.europa.ec.simpl.edcconnectoradapter.controller.mock;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europa.ec.simpl.data1.common.adapter.connector.model.transfer.TransferProcess;
import eu.europa.ec.simpl.edcconnectoradapter.model.edc.common.response.EdcAcknowledgementId;
import eu.europa.ec.simpl.edcconnectoradapter.model.edc.contract.response.EdcContractNegotiation;
import io.swagger.v3.oas.annotations.Hidden;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.codec.binary.Base64;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Hidden
@Log4j2
@RequiredArgsConstructor
@RestController
@RequestMapping("/mock/edc")
public class MockEDCController {

    private final ObjectMapper objectMapper;

    @PostMapping("/management/v3/assets")
    public ResponseEntity<JsonNode> assetsRegistration() throws JsonProcessingException {
        log.debug("assetsRegistration()");
        String jsonString = toRegistrationResponse();
        JsonNode jsonResponse = objectMapper.readTree(jsonString);
        return ResponseEntity.ok(jsonResponse);
    }

    @PostMapping("/management/v3/policydefinitions")
    public ResponseEntity<JsonNode> policyRegistration() throws JsonProcessingException {
        log.debug("policyRegistration()");
        String jsonString = toRegistrationResponse();
        JsonNode jsonResponse = objectMapper.readTree(jsonString);
        return ResponseEntity.ok(jsonResponse);
    }

    @PostMapping("/management/v3/contractdefinitions")
    public ResponseEntity<JsonNode> contractRegistration() throws JsonProcessingException {
        log.debug("contractRegistration()");
        String jsonString = toRegistrationResponse();
        JsonNode jsonResponse = objectMapper.readTree(jsonString);
        return ResponseEntity.ok(jsonResponse);
    }

    @PostMapping("/management/v3/catalog/request")
    public ResponseEntity<JsonNode> requestCatalog() throws JsonProcessingException {
        /* to test http error propagation
        if(true) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        */

        /* to test timeout error propagation */
        /*
        if (true) {
            try {
                Thread.sleep(30000);
            } catch (InterruptedException e) {
            }
        }
        */
        /**/

        String contractDefinitionId =
                Base64.encodeBase64String("961ead7f-d8b2-4fc6-8e94-e3efbe2127ea".getBytes(StandardCharsets.UTF_8));

        // do not modify the structure of the json
        String jsonString =
                """
           {
               "@id": "e5674c61-2808-44c5-9f32-a1dcb66020bd",
                "@type": "dcat:Catalog",
                "dcat:dataset": {
                    "@id": "ddb635ae-7ee7-44a5-813a-06b529702c0e-1",
                    "@type": "dcat:Dataset",
                    "odrl:hasPolicy":
                        {
                            "@id": "%s:Y29udHJhY3QtMTIzOmV4YW1wbGU6ZGF0YQ==:Y29udHJhY3QtMTIzOmV4YW1wbGU6ZGF0YQ==",
                            "odrl:assigner": "http://example.com/assigner1",
                            "contractDefinitionId": "contract-123"
                        }
                }
            }
        """
                        .formatted(contractDefinitionId);
        JsonNode jsonResponse = objectMapper.readTree(jsonString);
        return ResponseEntity.ok(jsonResponse);
    }

    @PostMapping("/management/v3/contractnegotiations")
    public ResponseEntity<EdcAcknowledgementId> initiateContractNegotiation() {

        // return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).build();

        return ResponseEntity.ok(
                EdcAcknowledgementId.builder().id(UUID.randomUUID().toString()).build());
    }

    @GetMapping("/management/v3/contractnegotiations/{contractNegotiationId}")
    public EdcContractNegotiation getContractNegotiation(@PathVariable String contractNegotiationId) {
        log.debug("getContractNegotiation() for contractNegotiationId {}", contractNegotiationId);
        return EdcContractNegotiation.builder()
                .contractAgreementId("contractAgreementId")
                .contractNegotiationId(contractNegotiationId)
                .counterPartyAddress("counterPartyAddress")
                .counterPartyId("counterPartyId")
                .createdAt(0)
                .protocol("protocol")
                .state("state")
                .type("type")
                .build();
    }

    @PostMapping("/management/v3/transferprocesses")
    public EdcAcknowledgementId startTransferProcess() {
        log.debug("startTransferProcess()");
        return EdcAcknowledgementId.builder().id(UUID.randomUUID().toString()).build();
    }

    @GetMapping("/management/v3/transferprocesses/{transferProcessId}")
    public TransferProcess getTransferProcess(@PathVariable String transferProcessId) {
        log.debug("getTransferProcess() for transferProcessId {}", transferProcessId);
        return TransferProcess.builder()
                .assetId("asset-id")
                .contractId("contract-id")
                .correlationId("correlation-id")
                .transferProcessId(transferProcessId)
                .state("COMPLETED")
                // .state("UNKNOWN")
                .stateTimestamp(System.currentTimeMillis())
                .build();
    }

    @PostMapping(
            value = "/management/v3/transferprocesses/{transferProcessId}/deprovision",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> deprovisioningTransferProcess(@PathVariable String transferProcessId) {
        log.debug("deprovisioningTransferProcess() for transferProcessId {}", transferProcessId);
        ResponseEntity<String> result;
        // all possible responses to be simulated
        // result = createDeprovisioningNotFoundErrorResult();
        // result = createDeprovisioningConflictErrorResult();
        result = createDeprovisioningSuccessResult();
        // result = createDeprovisioningErrorResult();
        return result;
    }

    protected ResponseEntity<String> createDeprovisioningSuccessResult() {
        return createSuccessResult(200, "(MOCK) TransferProcess with ID ... deprovisioned");
    }

    protected ResponseEntity<String> createDeprovisioningConflictErrorResult() {
        return createErrorResult(
                409,
                "(MOCK) Could not execute DeprovisionRequest on TransferProcess with ID ... because it's DEPROVISIONED",
                "ObjectConflict");
    }

    protected ResponseEntity<String> createDeprovisioningNotFoundErrorResult() {
        return createErrorResult(
                404, "(MOCK) Object of type TransferProcess with ID ... was not found", "ObjectNotFound");
    }

    protected ResponseEntity<String> createDeprovisioningErrorResult() {
        return createErrorResult(500, "(MOCK) INternal server error", "InternalServerError");
    }

    private String toRegistrationResponse() {
        return """
            {
                "@type": "IdResponse",
                "@id": "%s",
                "createdAt": 1749637003055,
                "@context": {
                    "@vocab": "https://w3id.org/edc/v0.0.1/ns/",
                    "edc": "https://w3id.org/edc/v0.0.1/ns/",
                    "odrl": "http://www.w3.org/ns/odrl/2/"
                }
            }
            """
                .formatted(UUID.randomUUID().toString());
    }

    private ResponseEntity<String> createSuccessResult(int httpStatus, String message) {
        JSONArray result = new JSONArray();
        JSONObject payload = new JSONObject();
        payload.put("message", message);
        payload.put("type", "OK");
        return ResponseEntity.status(httpStatus).body(result.toString());
    }

    private ResponseEntity<String> createErrorResult(int httpStatus, String message, String type) {
        JSONArray result = new JSONArray();
        JSONObject payload = new JSONObject();
        payload.put("message", message);
        payload.put("type", type);
        payload.putOpt("path", null);
        payload.putOpt("invalidValue", null);
        return ResponseEntity.status(httpStatus).body(result.toString());
    }
}
