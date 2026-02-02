package com.mcherm.versionedserialization;

import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.module.jsonSchema.JsonSchema;
import com.fasterxml.jackson.module.jsonSchema.JsonSchemaGenerator;
import com.github.victools.jsonschema.generator.MemberScope;
import com.github.victools.jsonschema.module.jackson.JacksonModule;
import com.github.victools.jsonschema.generator.Option;
import com.github.victools.jsonschema.generator.OptionPreset;
import com.github.victools.jsonschema.generator.SchemaGenerator;
import com.github.victools.jsonschema.generator.SchemaGeneratorConfig;
import com.github.victools.jsonschema.generator.SchemaGeneratorConfigBuilder;
import com.github.victools.jsonschema.generator.SchemaVersion;
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
     * This serializes an object to JSON, returning the JSON as a JsonNode.
     *
     * @param object the object to be serialized
     * @return the JSON as a JsonNode
     */
    public static JsonNode serializeAsNode(final Object object) {
        return objectMapper.valueToTree(object);
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
     * used by this project. The generated schema includes an "x-javaType" annotation
     * on each property node with the full generic type signature (e.g.
     * "java.util.List&lt;com.example.Widget&gt;").
     *
     * @return a configured SchemaGeneratorConfigBuilder
     */
    public static SchemaGeneratorConfigBuilder createVictoolsConfigBuilder() {
        final var configBuilder = new SchemaGeneratorConfigBuilder(
                SchemaVersion.DRAFT_2020_12, OptionPreset.PLAIN_JSON)
                .with(Option.DEFINITIONS_FOR_ALL_OBJECTS)
                .with(new JacksonModule());
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
     * Annotates a schema property node with x-javaType using a full generic type
     * signature (e.g. "java.util.List&lt;com.example.Widget&gt;"). Uses getDeclaredType()
     * so the original Java type is recorded (e.g. Optional rather than the unwrapped String).
     */
    private static void addJavaTypeAnnotation(final ObjectNode node, final MemberScope<?, ?> scope) {
        node.put("x-javaType", resolvedTypeToString(scope.getDeclaredType()));
    }

    /**
     * Recursively builds a full generic type signature string from a ResolvedType.
     * For example, {@code List<Money<USD>>} becomes
     * {@code "java.util.List<com.example.Money<com.example.USD>>"}.
     */
    private static String resolvedTypeToString(final ResolvedType resolvedType) {
        final String erasedName = resolvedType.getErasedType().getName();
        final var typeParams = resolvedType.getTypeParameters();
        if (typeParams == null || typeParams.isEmpty()) {
            return erasedName;
        }
        final StringBuilder sb = new StringBuilder(erasedName);
        sb.append('<');
        for (int i = 0; i < typeParams.size(); i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(resolvedTypeToString(typeParams.get(i)));
        }
        sb.append('>');
        return sb.toString();
    }

}
