package eu.europa.ec.simpl.edcconnectoradapter.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europa.ec.simpl.edcconnectoradapter.JacksonObjectValuesMapperUtil;
import java.util.Map;
import org.junit.jupiter.api.Test;

class JacksonObjectValuesMapperUtilTest {

    @Test
    void testGetMapperReturnsSameInstance() {
        ObjectMapper mapper1 = JacksonObjectValuesMapperUtil.getMapper();
        ObjectMapper mapper2 = JacksonObjectValuesMapperUtil.getMapper();
        assertSame(mapper1, mapper2); // Deve restituire sempre la stessa istanza
    }

    @Test
    void testToJsonStringConvertsObjectToJsonString() throws Exception {
        Map<String, String> map = Map.of("key", "value");
        String json = JacksonObjectValuesMapperUtil.toJsonString(map);
        assertEquals("{\"key\":\"value\"}", json);
    }

    @Test
    void testToJsonNodeConvertsObjectToJsonNode() throws Exception {
        Map<String, String> map = Map.of("key", "value");
        JsonNode node = JacksonObjectValuesMapperUtil.toJsonNode(map);
        assertEquals("value", node.get("key").asText());
    }

    @Test
    void testMapStringToObjectNodeParsesJsonStringToJsonNode() throws Exception {
        String json = "{\"key\":\"value\"}";
        JsonNode node = JacksonObjectValuesMapperUtil.mapStringToObjectNode(json);
        assertEquals("value", node.get("key").asText());
    }

    @Test
    void testMapStringToObjectNodeWithInvalidJsonThrowsException() {
        String invalidJson = "{key:value}"; // JSON non valido
        assertThrows(Exception.class, () -> JacksonObjectValuesMapperUtil.mapStringToObjectNode(invalidJson));
    }
}
