package eu.europa.ec.simpl.edcconnectoradapter.model.edc.management.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;
import java.util.Map;
import lombok.Data;

@Data
@JsonInclude(Include.NON_NULL)
public abstract class EdcAbstractManagement {

    @JsonProperty("@context")
    private final EdcContext context;

    private Map<String, String> properties;

    /*
     * @context": { "@vocab": "https://w3id.org/edc/v0.0.1/ns/" }
     */
    protected EdcAbstractManagement() {
        this.context = new EdcContext("https://w3id.org/edc/v0.0.1/ns/");
    }

    public void setProperty(String key, String value) {
        if (properties == null) {
            properties = new HashMap<>();
        }
        properties.put(key, value);
    }
}
