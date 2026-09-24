package eu.europa.ec.simpl.edcconnectoradapter.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europa.ec.simpl.data1.common.adapter.connector.model.contract.Constraint;
import eu.europa.ec.simpl.data1.common.adapter.connector.model.contract.ConstraintOperator;
import java.util.List;
import org.junit.jupiter.api.Test;

class JsonNodeObjectMapperUtilTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testValuesInJsonUsingJsonParserWithNullJsonReturnsEmptyList() {
        List<Constraint> result = JsonNodeObjectMapperUtil.getValuesInJsonUsingJsonParser(null);
        assertTrue(result.isEmpty());
    }

    @Test
    void testValuesInJsonUsingJsonParserWithEmptyArrayReturnsEmptyList() throws Exception {
        JsonNode emptyArray = objectMapper.readTree("[]");
        List<Constraint> result = JsonNodeObjectMapperUtil.getValuesInJsonUsingJsonParser(emptyArray);
        assertTrue(result.isEmpty());
    }

    @Test
    void testValuesInJsonUsingJsonParserWithSingleObjectReturnsListWithOneConstraint() throws Exception {
        String json =
                "{ \"odrl:leftOperand\": \"odrl:policy\", \"odrl:operator\": \"eq\", \"odrl:rightOperand\": \"value\" }";
        JsonNode node = objectMapper.readTree(json);

        List<Constraint> result = JsonNodeObjectMapperUtil.getValuesInJsonUsingJsonParser(node);
        assertEquals(1, result.size());
        assertEquals("policy", result.get(0).getLeftOperand());
        assertEquals(ConstraintOperator.EQ, result.get(0).getOperator());
        assertEquals("value", result.get(0).getRightOperand());
    }

    @Test
    void testValuesInJsonUsingJsonParserWithArrayReturnsListOfConstraints() throws Exception {
        String json =
                "[ { \"odrl:leftOperand\": \"odrl:policy\", \"odrl:operator\": \"eq\", \"odrl:rightOperand\": \"value1\" },"
                        + "{ \"odrl:leftOperand\": \"odrl:action\", \"odrl:operator\": \"neq\", \"odrl:rightOperand\": \"value2\" } ]";

        JsonNode node = objectMapper.readTree(json);
        List<Constraint> result = JsonNodeObjectMapperUtil.getValuesInJsonUsingJsonParser(node);

        assertEquals(2, result.size());
        assertEquals("policy", result.get(0).getLeftOperand());
        assertEquals(ConstraintOperator.EQ, result.get(0).getOperator());
        assertEquals("value1", result.get(0).getRightOperand());

        assertEquals("action", result.get(1).getLeftOperand());
        assertEquals(ConstraintOperator.NEQ, result.get(1).getOperator());
        assertEquals("value2", result.get(1).getRightOperand());
    }

    @Test
    void testJsonValueFromObjectWithDirectValueReturnsCorrectValue() throws Exception {
        JsonNode node = objectMapper.readTree("{ \"odrl:rightOperand\": \"value\" }");
        assertEquals("value", JsonNodeObjectMapperUtil.getJsonValueFromObject(node, "/odrl:rightOperand"));
    }

    @Test
    void testJsonValueFromObjectWithNestedObjectReturnsIdValue() throws Exception {
        JsonNode node = objectMapper.readTree("{ \"odrl:rightOperand\": { \"@id\": \"nestedValue\" } }");
        assertEquals("nestedValue", JsonNodeObjectMapperUtil.getJsonValueFromObject(node, "/odrl:rightOperand"));
    }

    @Test
    void testJsonValueWithMissingPropertyReturnsEmptyString() throws Exception {
        JsonNode node = objectMapper.readTree("{ \"someKey\": \"someValue\" }");
        assertEquals("", JsonNodeObjectMapperUtil.getJsonValue(node, "/missingKey"));
    }
}
