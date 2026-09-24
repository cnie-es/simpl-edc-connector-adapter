package eu.europa.ec.simpl.edcconnectoradapter.model.edc.transfer.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EdcTransferProcess {

    @JsonProperty("@id")
    private String transferProcessId;

    @JsonProperty("state")
    private String state;

    private String finalState;

    private String assetId;

    private String contractId;

    private String correlationId;

    private String transferType;

    private String errorDetail;

    private String type;

    private long stateTimestamp;
}
