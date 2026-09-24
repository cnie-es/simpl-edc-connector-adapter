package eu.europa.ec.simpl.edcconnectoradapter.model.edc.management.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class EdcContext {

    @JsonProperty("@vocab")
    private final String vocab;
}
