package eu.europa.ec.simpl.edcconnectoradapter.controller.v1.provider;

import eu.europa.ec.simpl.data1.common.adapter.connector.model.configuration.Participant;
import eu.europa.ec.simpl.data1.common.model.response.problem.BadRequestProblem;
import eu.europa.ec.simpl.data1.common.model.response.problem.InternalServerErrorProblem;
import eu.europa.ec.simpl.edcconnectoradapter.constant.RequestMappingV1;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping(RequestMappingV1.CONFIGURATION)
@Tag(name = "Config")
public interface ConfigurationController {

    @Operation(
            summary = "Returns the configured EDC participant",
            description = "Return the configured EDC participant to be used as Assigner in ODRL policies")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "OK",
                        content =
                                @Content(
                                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                                        schema = @Schema(implementation = Participant.class),
                                        examples =
                                                @ExampleObject(
                                                        name = "participant",
                                                        description = "participant",
                                                        value =
                                                                """
                                                                {
                                                                  "id": "simpl-edc-provider-01"
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
                                                              "instance": "/config/participant"
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
                                                              "instance": "/config/participant"
                                                            }
                                                            """))
                        })
            })
    @GetMapping(path = "/participant", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Participant> getParticipant() throws Exception;
}
