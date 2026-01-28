package com.mcherm.versionedserialization;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Contains public static methods for serialization and deserialization.
 */
public class SerializationUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * This serializes an object to JSON, returning the JSON as a string.
     *
     * @param object the object to be serialized
     * @return the JSON as a string
     */
    public static String serialize(final Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize object", e);
        }
    }

    /**
     * This deserializes an object from JSON, returning the new object.
     *
     * @param serialized the JSON as a string
     * @param clazz the class to deserialize to
     * @return the newly created object
     */
    public static <T> T deserialize(final String serialized, final Class<T> clazz) {
        try {
            return objectMapper.readValue(serialized, clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize object", e);
        }
    }
}
