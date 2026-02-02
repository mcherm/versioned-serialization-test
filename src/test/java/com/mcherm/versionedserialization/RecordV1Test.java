package com.mcherm.versionedserialization;

import com.mcherm.versionedserialization.objects.RecordV1;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Tests of serialization with the RecordV1 class (a Java record). */
public class RecordV1Test {

    @Test
    public void testRoundTrip() {
        final RecordV1 obj = new RecordV1("Alice", 95, List.of("math", "science"));

        final String serialized = SerializationUtil.serialize(obj);
        final String expected = """
            {"name":"Alice","score":95,"tags":["math","science"]}""";
        assertEquals(expected, serialized);

        final RecordV1 deserialized = SerializationUtil.deserialize(serialized, RecordV1.class);
        assertEquals("Alice", deserialized.name());
        assertEquals(95, deserialized.score());
        assertEquals(List.of("math", "science"), deserialized.tags());
    }

    @Test
    public void testGenerateSchemaJackson() {
        final String schema = SerializationUtil.generateSchemaJackson(RecordV1.class);
        final String expected = """
            {"type":"object",\
            "id":"urn:jsonschema:com:mcherm:versionedserialization:objects:RecordV1",\
            "properties":{\
            "name":{"type":"string"},\
            "score":{"type":"integer"},\
            "tags":{"type":"array","items":{"type":"string"}}\
            }}""";
        assertEquals(expected, schema);
    }

    @Test
    public void testGenerateSchemaVictools() {
        final String schema = SerializationUtil.generateSchemaVictools(RecordV1.class);
        final String expected = """
                {"$schema":"https://json-schema.org/draft/2020-12/schema",\
                "type":"object","properties":{\
                "name":{"type":"string","x-javaType":"java.lang.String"},\
                "score":{"type":"integer","x-javaType":"int"},\
                "tags":{"x-javaType":"java.util.List<java.lang.String>","type":"array","items":{"type":"string","x-javaType":"java.util.List<java.lang.String>"}}\
                }}""";
        assertEquals(expected, schema);
    }
}
