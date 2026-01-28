package com.mcherm.versionedserialization;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;

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
     * This generates a JSON Schema schema to document the serialization format
     * used if instances of the given class are serialized using JSON.
     *
     * @param clazz the class which will be serialized
     * @return the JSON Schema as a String
     */
    public static String generateSchema(final Class<?> clazz) {
        try {
            final JsonSchemaGenerator schemaGen = new JsonSchemaGenerator(objectMapper);
            final JsonSchema schema = schemaGen.generateSchema(clazz);
            return objectMapper.writeValueAsString(schema);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to generate schema", e);
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
