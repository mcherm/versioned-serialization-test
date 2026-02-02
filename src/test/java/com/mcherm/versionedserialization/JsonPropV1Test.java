package com.mcherm.versionedserialization;

import com.mcherm.versionedserialization.objects.JsonPropV1;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests of serialization with the JsonPropV1 class.
 * <p>
 * This class uses {@code @JsonProperty} to rename fields in the JSON output.
 * Jackson correctly serializes using the annotated names ("first_name", "last_name"),
 * but the Victools schema generator uses the Java field names ("firstName", "lastName")
 * instead. This mismatch means the schema does not accurately describe the actual
 * serialized JSON structure.
 */
public class JsonPropV1Test {

    @Test
    public void testRoundTrip() {
        final JsonPropV1 obj = new JsonPropV1();
        obj.firstName = "Alice";
        obj.lastName = "Smith";
        obj.age = 30;

        final String serialized = SerializationUtil.serialize(obj);
        // Note: Jackson uses the @JsonProperty names in the serialized output.
        final String expected = """
            {"age":30,"first_name":"Alice","last_name":"Smith"}""";
        assertEquals(expected, serialized);

        final JsonPropV1 deserialized = SerializationUtil.deserialize(serialized, JsonPropV1.class);
        assertEquals("Alice", deserialized.firstName);
        assertEquals("Smith", deserialized.lastName);
        assertEquals(30, deserialized.age);
    }

    @Test
    public void testGenerateSchemaJackson() {
        final String schema = SerializationUtil.generateSchemaJackson(JsonPropV1.class);
        // Jackson's schema generator correctly uses the @JsonProperty names.
        final String expected = """
            {"type":"object",\
            "id":"urn:jsonschema:com:mcherm:versionedserialization:objects:JsonPropV1",\
            "properties":{\
            "age":{"type":"integer"},\
            "first_name":{"type":"string"},\
            "last_name":{"type":"string"}\
            }}""";
        assertEquals(expected, schema);
    }

    @Test
    public void testGenerateSchemaVictools() {
        final String schema = SerializationUtil.generateSchemaVictools(JsonPropV1.class);
        // With JacksonModule registered, Victools now correctly uses the @JsonProperty names.
        final String expected = """
                {"$schema":"https://json-schema.org/draft/2020-12/schema",\
                "type":"object","properties":{\
                "age":{"type":"integer","x-javaType":"int"},\
                "first_name":{"type":"string","x-javaType":"java.lang.String"},\
                "last_name":{"type":"string","x-javaType":"java.lang.String"}\
                }}""";
        assertEquals(expected, schema);
    }
}
