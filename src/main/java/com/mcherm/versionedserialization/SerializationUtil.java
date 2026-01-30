package com.mcherm.versionedserialization;

import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;
import com.github.victools.jsonschema.generator.MemberScope;
import com.github.victools.jsonschema.generator.Option;
import com.github.victools.jsonschema.generator.OptionPreset;
import com.github.victools.jsonschema.generator.SchemaGenerator;
import com.github.victools.jsonschema.generator.SchemaGeneratorConfig;
import com.github.victools.jsonschema.generator.SchemaGeneratorConfigBuilder;
import com.github.victools.jsonschema.generator.SchemaVersion;
import com.github.victools.jsonschema.module.jackson.JacksonModule;
import com.github.victools.jsonschema.module.jackson.JacksonOption;
import com.mcherm.versionedserialization.objects.contents.Currency;

import java.util.Collections;

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

    /**
     * Reads a JSON string into arbitrary objects (JsonNode).
     *
     * @param json the JSON source, as a String
     * @return the parsed JsonNode for the root of the document
     */
    public static JsonNode deserializeAsNode(final String json) {
        try {
            return objectMapper.readTree(json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to read JSON", e);
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
        return generateSchemaVictools(clazz);
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
            final SchemaGeneratorConfigBuilder configBuilder = createVictoolsConfigBuilder();
            final SchemaGeneratorConfig config = configBuilder.build();
            final SchemaGenerator generator = new SchemaGenerator(config);
            final JsonNode schemaNode = generator.generateSchema(clazz);
            return objectMapper.writeValueAsString(schemaNode);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to generate schema", e);
        }
    }

    /**
     * Creates a victools SchemaGeneratorConfigBuilder with the standard configuration
     * used by this project. The generated schema includes "x-javaType" and
     * "x-javaElementType" annotations on each property node, recording the declared
     * Java type that produces that part of the schema.
     *
     * @return a configured SchemaGeneratorConfigBuilder
     */
    public static SchemaGeneratorConfigBuilder createVictoolsConfigBuilder() {
        final var configBuilder = new SchemaGeneratorConfigBuilder(
                SchemaVersion.DRAFT_2020_12, OptionPreset.PLAIN_JSON)
                .with(Option.DEFINITIONS_FOR_ALL_OBJECTS);
        // Tell Victools about the custom serialization for Currency objects
        configBuilder.forFields().withTargetTypeOverridesResolver(
                field -> {
                    final ResolvedType fieldType = field.getType();
                    if (fieldType != null && Currency.class.isAssignableFrom(fieldType.getErasedType())) {
                        return Collections.singletonList(field.getContext().resolve(String.class));
                    }
                    return null;
                }
        );
        // Annotate each property node with the declared Java type
        configBuilder.forFields().withInstanceAttributeOverride(
                (node, field, context) -> addJavaTypeAnnotation(node, field)
        );
        configBuilder.forMethods().withInstanceAttributeOverride(
                (node, method, context) -> addJavaTypeAnnotation(node, method)
        );
        return configBuilder;
    }

    /**
     * Annotates a schema property node with x-javaType (and x-javaElementType for
     * parameterized types like List&lt;T&gt;). Uses getDeclaredType() so the original
     * Java type is recorded (e.g. Optional rather than the unwrapped String).
     */
    private static void addJavaTypeAnnotation(final ObjectNode node, final MemberScope<?, ?> scope) {
        final var declaredType = scope.getDeclaredType();
        node.put("x-javaType", declaredType.getErasedType().getName());
        final var typeParams = declaredType.getTypeParameters();
        if (typeParams != null && !typeParams.isEmpty()) {
            node.put("x-javaElementType", typeParams.get(0).getErasedType().getName());
        }
    }

}
