package eu.europa.ec.simpl.edcconnectoradapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class JacksonObjectValuesMapperUtil {

    private static final ObjectMapper mapper = new ObjectMapper();

    private JacksonObjectValuesMapperUtil() {}

    public static ObjectMapper getMapper() {
        return mapper;
    }

    public static String toJsonString(Object obj) throws JsonProcessingException {
        return mapper.writeValueAsString(obj);
    }

    public static JsonNode toJsonNode(Object obj) throws JsonProcessingException {
        return mapper.readTree(toJsonString(obj));
    }

    public static JsonNode mapStringToObjectNode(String jstr) throws JsonProcessingException {
        return mapper.readTree(jstr);
    }
}
