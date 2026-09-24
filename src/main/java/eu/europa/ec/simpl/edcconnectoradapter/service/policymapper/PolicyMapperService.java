package eu.europa.ec.simpl.edcconnectoradapter.service.policymapper;

import com.fasterxml.jackson.databind.JsonNode;
import eu.europa.ec.simpl.data1.common.adapter.connector.model.catalog.Policy;

public interface PolicyMapperService {

    Policy mapPolicyConstraint(JsonNode policyObject);
}
