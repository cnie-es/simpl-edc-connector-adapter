package eu.europa.ec.simpl.edcconnectoradapter.controller.v1.consumer;

import eu.europa.ec.simpl.data1.common.adapter.connector.model.transfer.TransferProcess;
import eu.europa.ec.simpl.data1.common.adapter.connector.model.transfer.TransferProcessId;
import eu.europa.ec.simpl.data1.common.adapter.connector.model.transfer.TransferRequest;
import eu.europa.ec.simpl.data1.common.model.response.problem.BadRequestProblem;
import eu.europa.ec.simpl.data1.common.model.response.problem.InternalServerErrorProblem;
import eu.europa.ec.simpl.edcconnectoradapter.constant.RequestMappingV1;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping(RequestMappingV1.TRANSFER_PROCESS)
@Tag(name = "Transfer")
public interface TransferProcessController {

    @Operation(
            summary = "Initiate a new transfer process",
            description =
                    "Starts a data transfer process using a previously negotiated contract. This operation enables the actual movement of data from provider to consumer.")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Successful operation",
                        content = {
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = TransferProcessId.class),
                                    examples =
                                            @ExampleObject(
                                                    name = "transferProcessId",
                                                    description = "transferProcessId",
                                                    value =
                                                            """
                                                                    {
                                                                      "transferProcessId": "transfer-process-567890"
                                                                    }
                                                                    """))
                        }),
                @ApiResponse(
                        responseCode = "400",
                        description = "Bad Request",
                        content = {
                            @Content(
                                    mediaType = "application/problem+json",
                                    schema = @Schema(implementation = BadRequestProblem.class),
                                    examples =
                                            @ExampleObject(
                                                    name = "Bad Request Error Example",
                                                    description = "Bad Request Error Example",
                                                    value =
                                                            """
                                                                    {
                                                                      "type": "urn:problem-type:simpl:validationError",
                                                                      "title": "Invalid payload",
                                                                      "status": 400,
                                                                      "detail": "missing required arguments",
                                                                      "issues": [
                                                                        {
                                                                          "detail": "Field 'dataDestination' must not be empty"
                                                                        }
                                                                      ],
                                                                      "instance": "/transfers"
                                                                    }
                                                                    """))
                        }),
                @ApiResponse(
                        responseCode = "500",
                        description = "Internal Server Error",
                        content = {
                            @Content(
                                    mediaType = "application/problem+json",
                                    schema = @Schema(implementation = InternalServerErrorProblem.class),
                                    examples =
                                            @ExampleObject(
                                                    name = "Internal Server Error Example",
                                                    description = "Internal Server Error Example",
                                                    value =
                                                            """
                                                                    {
                                                                      "type": "urn:problem-type:simpl:internalServerError",
                                                                      "title": "Internal Server Error",
                                                                      "status": 500,
                                                                      "detail": "Unexpected internal error",
                                                                      "instance": "/transfers"
                                                                    }
                                                                    """))
                        })
            })
    @PostMapping()
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content =
                    @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TransferRequest.class),
                            examples =
                                    @ExampleObject(
                                            name = "transferRequest",
                                            description = "transferRequest",
                                            value =
                                                    """
                                            {
                                              "contractId": "4a7f28e5-33a9-4f9e-9de9-a4f687502bce",
                                              "dataDestination": {
                                                "bucketName": "simpl-cons",
                                                "keyName": "test-key-name",
                                                "objectName": "european_health_data.csv",
                                                "path": "folder1/",
                                                "region": "de",
                                                "storage": "s3-eu-central-1.ionoscloud.com",
                                                "type": "IonosS3"
                                              },
                                              "providerEndpoint": "https://edc-provider.dev.simpl-europe.eu/protocol",
                                              "properties": {
                                                "resourceAddressTemplateId": "TPL-1",
                                                "key2": "value2"
                                              }
                                            }
                                            """)))
    ResponseEntity<TransferProcessId> startTransfer(@Valid @RequestBody TransferRequest request) throws Exception;

    @Operation(
            summary = "Returns the transfer process status",
            description =
                    "Returns the current status of a data transfer process. Allows consumers to monitor the progress and completion of data delivery.")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Successful operation",
                        content = {
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = TransferProcess.class),
                                    examples =
                                            @ExampleObject(
                                                    name = "transferProcess",
                                                    description = "transferProcess",
                                                    value =
                                                            """
                                                                    {
                                                                      "id": "transfer-process-567890",
                                                                      "state": "COMPLETED",
                                                                      "type": "CONSUMER",
                                                                      "errorDetail": null,
                                                                      "dataDestination": {
                                                                        "type": "HttpData",
                                                                        "baseUrl": "https://consumer-destination.example.com"
                                                                      },
                                                                      "contractAgreementId": "agreement-xyz789"
                                                                    }
                                                                    """))
                        }),
                @ApiResponse(
                        responseCode = "400",
                        description = "Bad Request",
                        content = {
                            @Content(
                                    mediaType = "application/problem+json",
                                    schema = @Schema(implementation = BadRequestProblem.class),
                                    examples =
                                            @ExampleObject(
                                                    name = "Bad Request Error Example",
                                                    description = "Bad Request Error Example",
                                                    value =
                                                            """
                                                                    {
                                                                      "type": "urn:problem-type:simpl:validationError",
                                                                      "title": "Invalid payload",
                                                                      "status": 400,
                                                                      "detail": "missing required arguments",
                                                                      "issues": [
                                                                        {
                                                                          "detail": "Field 'transferId' must not be empty"
                                                                        }
                                                                      ],
                                                                      "instance": "/transfers/{transferProcessId}"
                                                                    }
                                                                    """))
                        }),
                @ApiResponse(
                        responseCode = "404",
                        description = "NotFound",
                        content = {
                            @Content(
                                    mediaType = "application/problem+json",
                                    examples =
                                            @ExampleObject(
                                                    name = "Unauthorized Error Example",
                                                    description = "Unauthorized Error Example",
                                                    value =
                                                            """
                                                                    {
                                                                      "type": "urn:problem-type:simpl:unauthorized",
                                                                      "title": "Unauthorized",
                                                                      "status": 404,
                                                                      "detail": "Missing or invalid Authorization header",
                                                                      "instance": "/transfers/{transferProcessId}"
                                                                    }
                                                                    """))
                        }),
                @ApiResponse(
                        responseCode = "500",
                        description = "Internal Server Error",
                        content = {
                            @Content(
                                    mediaType = "application/problem+json",
                                    schema = @Schema(implementation = InternalServerErrorProblem.class),
                                    examples =
                                            @ExampleObject(
                                                    name = "Internal Server Error Example",
                                                    description = "Internal Server Error Example",
                                                    value =
                                                            """
                                                                    {
                                                                      "type": "urn:problem-type:simpl:internalServerError",
                                                                      "title": "Internal Server Error",
                                                                      "status": 500,
                                                                      "detail": "Unexpected internal error",
                                                                      "instance": "/transfers/{transferProcessId}"
                                                                    }
                                                                    """))
                        })
            })
    @GetMapping("/{transferProcessId}")
    ResponseEntity<TransferProcess> getTransferStatus(
            @Parameter(description = "The ID of the transfer process to retrieve the status for", required = true)
                    @PathVariable("transferProcessId")
                    String transferProcessId)
            throws Exception;
}
