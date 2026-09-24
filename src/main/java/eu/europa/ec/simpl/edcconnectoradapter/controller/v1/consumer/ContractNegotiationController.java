package eu.europa.ec.simpl.edcconnectoradapter.controller.v1.consumer;

import eu.europa.ec.simpl.data1.common.adapter.connector.model.catalog.CatalogSearchResult;
import eu.europa.ec.simpl.data1.common.adapter.connector.model.contract.ContractNegotiation;
import eu.europa.ec.simpl.data1.common.adapter.connector.model.contract.ContractNegotiationId;
import eu.europa.ec.simpl.data1.common.adapter.connector.model.contract.ContractNegotiationRequest;
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

@RequestMapping(RequestMappingV1.BASE)
@Tag(name = "Contract")
public interface ContractNegotiationController {

    @Operation(
            summary =
                    "Returns detailed information about the asset available in the connector's catalog, useful for initiating a contract negotiation.",
            description =
                    "Allows a consumer to extract detailed information about a specific asset of interest from the provider connector's catalog. This provides the preliminary information necessary to initiate the contract negotiation phase.")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Successful operation",
                        content = {
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = CatalogSearchResult.class),
                                    examples =
                                            @ExampleObject(
                                                    name = "catalogResponse",
                                                    description = "catalogResponse",
                                                    value =
                                                            """
                                                                    {
                                                                      "offers": [
                                                                        {
                                                                          "assetId": "asset-123-xyz",
                                                                          "offerId": "offer-456-abc",
                                                                          "policy": {
                                                                            "id": "policy-789-def",
                                                                            "policyConstraints": [
                                                                              {
                                                                                "condition": "PURPOSE",
                                                                                "conditionOperator": "EQ",
                                                                                "conditionValue": "data-sharing"
                                                                              }
                                                                            ],
                                                                            "type": "USAGE"
                                                                          },
                                                                          "providerEndpointUrl": "https://provider-edc.example.com/api/v1/dsp",
                                                                          "providerParticipantId": "provider-id-123456"
                                                                        },
                                                                        {
                                                                          "assetId": "asset-789-uvw",
                                                                          "offerId": "offer-321-rst",
                                                                          "policy": {
                                                                            "id": "policy-654-ghi",
                                                                            "policyConstraints": [],
                                                                            "type": "ACCESS"
                                                                          },
                                                                          "providerEndpointUrl": "https://another-provider.example.com/api/v1/dsp",
                                                                          "providerParticipantId": "provider-id-987654"
                                                                        }
                                                                      ]
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
                                                                          "detail": "Field 'assetId' must not be empty"
                                                                        }
                                                                      ],
                                                                      "instance": "/catalog/assetDetails"
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
                                                                      "instance": "/catalog/assetDetails"
                                                                    }
                                                                    """))
                        })
            })
    @PostMapping("/connectorCatalog/assets")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content =
                    @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ContractNegotiationRequest.class),
                            examples =
                                    @ExampleObject(
                                            name = "ContractNegotiationRequest",
                                            description = "ContractNegotiationRequest",
                                            value =
                                                    """
                                    { "assetId": "asset-123", "contractDefinitionId": "961ead7f-d8b2-4fc6-8e94-e3efbe2127ea", "providerEndpoint": "http://endpoint.provider.com" }
                                    """)))
    ResponseEntity<CatalogSearchResult> searchCatalogOffers(@Valid @RequestBody ContractNegotiationRequest request);

    @Operation(
            summary = "Initiate a new contract negotiation",
            description =
                    "Initiates a contract negotiation between a consumer and a provider for a selected data offer. This step is required before any data transfer can occur.")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Successful operation",
                        content = {
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ContractNegotiationId.class),
                                    examples =
                                            @ExampleObject(
                                                    name = "contractNegotiationId",
                                                    description = "contractNegotiationId",
                                                    value =
                                                            """
                                                                    {
                                                                      "contractNegotiationId": "negotiation-abcd1234"
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
                                                                          "detail": "Field 'assetId' must not be empty"
                                                                        }
                                                                      ],
                                                                      "instance": "/contracts"
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
                                                                      "instance": "/contracts"
                                                                    }
                                                                    """))
                        })
            })
    @PostMapping("/contracts")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content =
                    @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ContractNegotiationRequest.class),
                            examples =
                                    @ExampleObject(
                                            name = "ContractNegotiationRequest",
                                            description = "ContractNegotiationRequest",
                                            value =
                                                    """
                                    { "assetId": "asset-123", "contractDefinitionId": "961ead7f-d8b2-4fc6-8e94-e3efbe2127ea", "providerEndpoint": "http://endpoint.provider.com" }
                                    """)))
    ResponseEntity<ContractNegotiationId> startContractNegotiation(
            @Valid @RequestBody ContractNegotiationRequest request);

    @Operation(
            summary = "Returns the contract negotiation status",
            description =
                    "Retrieves the current status of a contract negotiation. Useful for tracking the progress and outcome of the negotiation process.")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Successful operation",
                        content = {
                            @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ContractNegotiation.class),
                                    examples =
                                            @ExampleObject(
                                                    name = "contractNegotiation",
                                                    description = "contractNegotiation",
                                                    value =
                                                            """
                                                                    {
                                                                      "id": "negotiation-abcd1234",
                                                                      "state": "CONFIRMED",
                                                                      "counterPartyId": "provider-edc-123",
                                                                      "counterPartyAddress": "https://provider-edc.example.com",
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
                                                                          "detail": "Field 'contractId' must not be empty"
                                                                        }
                                                                      ],
                                                                      "instance": "/contracts/{contractNegotiationId}"
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
                                                                      "instance": "/contracts/{contractNegotiationId}"
                                                                    }
                                                                    """))
                        })
            })
    @GetMapping("/contracts/{contractNegotiationId}")
    ResponseEntity<ContractNegotiation> getContractNegotiationStatus(
            @Parameter(description = "The ID of the contract negotiation to retrieve the status for", required = true)
                    @PathVariable("contractNegotiationId")
                    String contractNegotiationId);
}
