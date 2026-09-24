package eu.europa.ec.simpl.edcconnectoradapter.model.edc.management.request;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class EdcAssetDefinition extends EdcAbstractManagement {

    private JsonNode dataAddress;

    public EdcAssetDefinition() {
        // empty properties element is mandatory for as asset registration on EDC
        super.setProperties(new HashMap<>());
    }

    /**
     * Override the setProperties method to ensure that null values are not added to the properties map
     * and properies is never null (as it is mandatory for asset registration on EDC)
     */
    @Override
    public void setProperties(Map<String, String> properties) {
        if (properties != null) {
            properties.forEach((k, v) -> {
                if (v != null) {
                    setProperty(k, v);
                }
            });
        }
    }
}
