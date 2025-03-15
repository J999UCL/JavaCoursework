package uk.ac.ucl.model;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonUtil {
    // Create a single ObjectMapper instance
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Serializes the given object to a JSON string.
     */
    public static String serialize(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }

    /**
     * Deserializes the given JSON string to an object of the specified class.
     */
    public static <T> T deserialize(String json, Class<T> clazz) throws Exception {
        return objectMapper.readValue(json, clazz);
    }
}
