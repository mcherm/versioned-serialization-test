package com.mcherm.versionedserialization;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;
import com.github.victools.jsonschema.generator.OptionPreset;
import com.github.victools.jsonschema.generator.SchemaGenerator;
import com.github.victools.jsonschema.generator.SchemaGeneratorConfig;
import com.github.victools.jsonschema.generator.SchemaGeneratorConfigBuilder;
import com.github.victools.jsonschema.generator.SchemaVersion;
import com.github.victools.jsonschema.module.jackson.JacksonModule;

/**
 * Contains public static methods for serialization and deserialization.
 */
public class SerializationUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

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
        return generateSchemaJackson(clazz);
    }

    /**
     * This generates a JSON Schema schema to document the serialization format
     * used if instances of the given class are serialized using JSON.
     *
     * @param clazz the class which will be serialized
     * @return the JSON Schema as a String
     */
    static String generateSchemaJackson(final Class<?> clazz) {
        try {
            final JsonSchemaGenerator schemaGen = new JsonSchemaGenerator(objectMapper);
            final JsonSchema schema = schemaGen.generateSchema(clazz);
            return objectMapper.writeValueAsString(schema);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to generate schema", e);
        }
    }

    /**
     * This generates a JSON Schema schema to document the serialization format
     * used if instances of the given class are serialized using JSON.
     *
     * @param clazz the class which will be serialized
     * @return the JSON Schema as a String
     */
    static String generateSchemaVictools(final Class<?> clazz) {
        try {
            final SchemaGeneratorConfigBuilder configBuilder = new SchemaGeneratorConfigBuilder(SchemaVersion.DRAFT_2020_12, OptionPreset.PLAIN_JSON);
            configBuilder.with(new JacksonModule());
            final SchemaGeneratorConfig config = configBuilder.build();
            final SchemaGenerator generator = new SchemaGenerator(config);
            final JsonNode schemaNode = generator.generateSchema(clazz);
            return objectMapper.writeValueAsString(schemaNode);
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
