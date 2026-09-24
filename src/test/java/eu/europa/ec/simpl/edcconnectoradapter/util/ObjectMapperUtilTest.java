package eu.europa.ec.simpl.edcconnectoradapter.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
import org.junit.jupiter.api.Test;

class ObjectMapperUtilTest {

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testHeadersToListWithNullHeadersReturnsEmptyList() {
        assertTrue(ObjectMapperUtil.headersToList(null).isEmpty());
    }

    @Test
    void testHeadersToListWithHeadersReturnsFormattedList() {
        Headers headers = mock(Headers.class);
        Header header1 = mock(Header.class);
        Header header2 = mock(Header.class);

        when(header1.key()).thenReturn("Content-Type");
        when(header1.value()).thenReturn("application/json".getBytes());
        when(header2.key()).thenReturn("Authorization");
        when(header2.value()).thenReturn("Bearer token".getBytes());

        when(headers.iterator()).thenReturn(List.of(header1, header2).iterator());

        List<String> result = ObjectMapperUtil.headersToList(headers);
        assertEquals(2, result.size());
        assertEquals("Content-Type:application/json", result.get(0));
        assertEquals("Authorization:Bearer token", result.get(1));
    }

    @Test
    void testGetObjectFromPayloadValidJsonReturnsObject() throws IOException {
        String json = "{\"name\":\"John\",\"age\":30}";
        Person person = ObjectMapperUtil.getObjectFromPayload(json, Person.class, objectMapper);
        assertEquals("John", person.getName());
        assertEquals(30, person.getAge());
    }

    @Test
    void testGetObjectFromPayloadInvalidJson() {
        String invalidJson = "{name:John, age:30}";
        assertThrows(
                JsonProcessingException.class,
                () -> ObjectMapperUtil.getObjectFromPayload(invalidJson, Person.class, objectMapper));
    }

    static class Person {
        private String name;
        private int age;

        public String getName() {
            return name;
        }

        public int getAge() {
            return age;
        }
    }
}
