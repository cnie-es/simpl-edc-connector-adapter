package eu.europa.ec.simpl.edcconnectoradapter.controller.v1.provider;

import eu.europa.ec.simpl.data1.common.enumeration.OfferType;
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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RequestMapping(RequestMappingV1.SELF_DESCRIPTIONS)
@Tag(name = "Self Description")
public interface RegistrationController {

    @Operation(
            summary = "Register a SD json and return it enriched by EDC registration data",
            description =
                    "Registers asset and policies into the provider EDC, creates a contract definition on EDC connector")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Successfully registered SD json",
                        content =
                                @Content(
                                        mediaType = "application/json",
                                        schema = @Schema(implementation = String.class),
                                        examples =
                                                @ExampleObject(
                                                        name = "SD json file registered",
                                                        description = "SD json file registered",
                                                        value =
                                                                """
                                                                {
                                                                  "@context": {
                                                                    "dcat": "http://www.w3.org/ns/dcat#",
                                                                    "edc": "https://w3id.org/edc/v0.0.1/ns/"
                                                                  },
                                                                  "@id": "urn:uuid:9b220110-4482-2113-a786-6ace13b000ee",
                                                                  "@type": "dcat:Dataset",
                                                                  "edc:assetId": "asset-9876",
                                                                  "edc:contractDefinitionId": "contract-def-45678",
                                                                  "edc:providerId": "simpl-edc-provider-01"
                                                                }
                                                                """))),
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
                                                              "instance": "/selfDescriptions/enriched",
                                                              "issues": [
                                                                {
                                                                  "detail": "Field 'json-ld' must not be empty"
                                                                }
                                                              ]
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
                                                              "instance": "/selfDescriptions/enriched"
                                                            }
                                                            """))
                        })
            })
    @PostMapping(
            path = "/enriched",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content =
                    @Content(
                            mediaType = "application/json",
                            schema =
                                    @Schema(
                                            type = "object",
                                            requiredProperties = {"sdJson"}),
                            examples =
                                    @ExampleObject(
                                            name = "sdJsonRequest",
                                            value =
                                                    """
                                                    {
                                                        "@context": "http://www.w3.org/ns/odrl.jsonld",
                                                        "@type": "Set",
                                                        "assigner": {
                                                          "role": "http://www.w3.org/ns/odrl/2/assigner",
                                                          "uid": "provider"
                                                        },
                                                        "permission": [
                                                          {
                                                            "action": [
                                                              "http://simpl.eu/odrl/actions/search"
                                                            ],
                                                            "assignee": {
                                                              "role": "http://www.w3.org/ns/odrl/2/assignee",
                                                              "uid": "CONSUMER"
                                                            },
                                                            "constraint": [
                                                              {
                                                                "leftOperand": "http://www.w3.org/ns/odrl/2/dateTime",
                                                                "operator": "http://www.w3.org/ns/odrl/2/gteq",
                                                                "rightOperand": "2024-08-01T00:00:00Z"
                                                              },
                                                              {
                                                                "leftOperand": "http://www.w3.org/ns/odrl/2/dateTime",
                                                                "operator": "http://www.w3.org/ns/odrl/2/lteq",
                                                                "rightOperand": "2024-08-31T23:59:59Z"
                                                              }
                                                            ],
                                                            "target": ""
                                                          }
                                                        ],
                                                        "profile": "http://www.w3.org/ns/odrl/2/odrl.jsonld",
                                                        "target": "",
                                                        "uid": "68777919-f260-4305-9c41-d4266ac0a639"
                                                      }

                                                    """)))
    @Deprecated(since = "v1.14.0", forRemoval = true)
    /**
     * @deprecated to be removed when enrichAndValidate on SdTooling SD Controller V2 will be removed
     */
    ResponseEntity<String> register(
            @Valid @RequestBody String jsonFile,
            @Parameter(description = "offering type", required = true, example = "DATA") @RequestParam
                    OfferType offeringType)
            throws Exception;
}
