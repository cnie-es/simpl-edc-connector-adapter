package eu.europa.ec.simpl.edcconnectoradapter.service.policymapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europa.ec.simpl.data1.common.adapter.connector.model.catalog.Policy;
import eu.europa.ec.simpl.data1.common.adapter.connector.model.catalog.PolicyConstraint;
import eu.europa.ec.simpl.edcconnectoradapter.JacksonObjectValuesMapperUtil;
import eu.europa.ec.simpl.edcconnectoradapter.TestConstants;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PolicyMapperServiceTest {

    private ObjectMapper objectMapper = new ObjectMapper();
    private PolicyMapperService policyMapperService;

    @BeforeEach
    void setUpEach() {
        policyMapperService = new PolicyMapperServiceImpl();
    }

    @Test
    void testMapPolicyConstraint() throws Exception {

        PolicyConstraint countPolicyConstraint = PolicyConstraint.builder()
                .condition("count")
                .conditionOperator("less than or equal to")
                .conditionValue("10")
                .build();

        JsonNode offerPolicy = JacksonObjectValuesMapperUtil.mapStringToObjectNode(TestConstants.OFFER_POLICY);
        Policy policy = policyMapperService.mapPolicyConstraint(offerPolicy);

        assertNotNull(policy);

        List<PolicyConstraint> constraints = policy.getPolicyConstraints();
        assertEquals(3, constraints.size());
        assertTrue(constraints.contains(countPolicyConstraint));
    }

    @Test
    void testMapPolicyConstraintWithPermissionAsObjectWithConstraint() throws Exception {
        String json =
                """
        {
          "odrl:permission": {
            "odrl:constraint": {
              "odrl:leftOperand": "purpose",
              "odrl:operator": "eq",
              "odrl:rightOperand": "research"
            }
          }
        }
        """;
        JsonNode node = objectMapper.readTree(json);

        Policy policy = policyMapperService.mapPolicyConstraint(node);
        assertEquals(1, policy.getPolicyConstraints().size());
        assertEquals("purpose", policy.getPolicyConstraints().get(0).getCondition());
    }

    @Test
    void testMapPolicyConstraintWithMissingPermissionNode() throws Exception {
        String json = "{}";
        JsonNode node = objectMapper.readTree(json);

        Policy policy = policyMapperService.mapPolicyConstraint(node);
        assertNotNull(policy);
        assertTrue(policy.getPolicyConstraints().isEmpty());
    }

    @Test
    void testMapPolicyConstraintWithEmptyPermissionArray() throws Exception {
        String json = """
        {
          "odrl:permission": []
        }
        """;
        JsonNode node = objectMapper.readTree(json);

        Policy policy = policyMapperService.mapPolicyConstraint(node);
        assertTrue(policy.getPolicyConstraints().isEmpty());
    }

    @Test
    void testMapPolicyConstraintWithPermissionArrayWithMultipleConstraints() throws Exception {
        String json =
                """
        {
          "odrl:permission": [
            {
              "odrl:constraint": {
                "odrl:leftOperand": "purpose",
                "odrl:operator": "eq",
                "odrl:rightOperand": "research"
              }
            },
            {
              "odrl:constraint": {
                "odrl:leftOperand": "role",
                "odrl:operator": "eq",
                "odrl:rightOperand": "admin"
              }
            }
          ]
        }
        """;
        JsonNode node = objectMapper.readTree(json);

        Policy policy = policyMapperService.mapPolicyConstraint(node);
        List<PolicyConstraint> constraints = policy.getPolicyConstraints();
        assertEquals(2, constraints.size());
        assertEquals("purpose", constraints.get(0).getCondition());
        assertEquals("role", constraints.get(1).getCondition());
    }

    @Test
    void testMapPolicyConstraintWithPermissionWithNoConstraint() throws Exception {
        String json =
                """
        {
          "odrl:permission": {
            "odrl:action": "use"
          }
        }
        """;
        JsonNode node = objectMapper.readTree(json);

        Policy policy = policyMapperService.mapPolicyConstraint(node);
        assertTrue(policy.getPolicyConstraints().isEmpty());
    }
}
