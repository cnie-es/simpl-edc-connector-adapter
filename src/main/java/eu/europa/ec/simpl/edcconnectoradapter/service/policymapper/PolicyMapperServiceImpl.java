package eu.europa.ec.simpl.edcconnectoradapter.service.policymapper;

import com.fasterxml.jackson.databind.JsonNode;
import eu.europa.ec.simpl.data1.common.adapter.connector.model.catalog.Policy;
import eu.europa.ec.simpl.data1.common.adapter.connector.model.catalog.PolicyConstraint;
import eu.europa.ec.simpl.data1.common.adapter.connector.model.contract.Constraint;
import eu.europa.ec.simpl.edcconnectoradapter.util.JsonNodeObjectMapperUtil;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor
public class PolicyMapperServiceImpl implements PolicyMapperService {

    @Override
    @SneakyThrows
    public Policy mapPolicyConstraint(JsonNode policyObject) {
        return Policy.builder()
                .policyConstraints(convertPolicyAsUI(policyObject, "/odrl:permission"))
                .build();
    }

    @SneakyThrows
    private List<PolicyConstraint> convertPolicyAsUI(JsonNode policyObject, String path) {
        JsonNode permissions = policyObject.at(path);
        List<PolicyConstraint> policyConstraints = new ArrayList<>();
        if (!permissions.isMissingNode() && permissions.isArray()) {
            permissions.forEach(permission -> policyConstraints.addAll(convertPolicy(permission)));
            return policyConstraints;
        } else {
            return convertPolicy(permissions);
        }
    }

    private static List<PolicyConstraint> convertPolicy(JsonNode permission) {
        List<PolicyConstraint> policyConstraints = new ArrayList<>();
        JsonNode constraints = permission.at("/odrl:constraint");
        if (!constraints.isMissingNode()) {
            List<Constraint> constraintList = JsonNodeObjectMapperUtil.getValuesInJsonUsingJsonParser(constraints);
            constraintList.stream().forEach(constraint -> {
                PolicyConstraint policyConstraint = PolicyConstraint.builder()
                        .condition(constraint.getLeftOperand())
                        .conditionOperator(constraint.getOperator().getValue())
                        .conditionValue(constraint.getRightOperand().toString())
                        .build();
                policyConstraints.add(policyConstraint);
            });
        }
        return policyConstraints;
    }
}
