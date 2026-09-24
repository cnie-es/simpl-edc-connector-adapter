package eu.europa.ec.simpl.edcconnectoradapter.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;

@Log4j2
public final class ObjectMapperUtil {

    private ObjectMapperUtil() {}

    public static List<String> headersToList(Headers headers) {
        List<String> headersList = new ArrayList<>();

        if (headers != null) {
            for (Header header : headers) {
                headersList.add(header.key() + ":" + StringUtils.toEncodedString(header.value(), null));
            }
        }

        return headersList;
    }

    public static <T> T getObjectFromPayload(Object payload, Class<T> classType, ObjectMapper objectMapper)
            throws JsonProcessingException {
        try {
            return objectMapper.readValue((String) payload, classType);
        } catch (JsonProcessingException e) {
            log.error("getObjectFromPayload() failed parsing the payload: {}", e.getMessage());
            throw e;
        }
    }
}
