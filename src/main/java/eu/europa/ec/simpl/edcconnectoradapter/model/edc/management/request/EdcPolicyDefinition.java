package eu.europa.ec.simpl.edcconnectoradapter.model.edc.management.request;

import eu.europa.ec.simpl.data1.common.model.ld.odrl.OdrlPolicy;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class EdcPolicyDefinition extends EdcAbstractManagement {
    /*
     * { "@context": { "@vocab": "https://w3id.org/edc/v0.0.1/ns/" }, "policy": {...} }
     */

    private final OdrlPolicy policy;
}
