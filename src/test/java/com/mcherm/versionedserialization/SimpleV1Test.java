package com.mcherm.versionedserialization;

import com.mcherm.versionedserialization.objects.SimpleV1;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Tests of serialization with the SimpleV1 class. */
public class SimpleV1Test {

    @Test
    public void testRoundTrip() {
        final SimpleV1 obj = new SimpleV1();
        obj.s = "abc";
        obj.i = 37;

        final String serialized = SerializationUtil.serialize(obj);
        final String expected = "{\"s\":\"abc\",\"i\":37}";
        assertEquals(expected, serialized);

        final SimpleV1 deserialized = SerializationUtil.deserialize(serialized, SimpleV1.class);
        assertEquals("abc", deserialized.s);
        assertEquals(37, deserialized.i);
    }

    @Test
    public void testGenerateSchemaJackson() {
        final String schema = SerializationUtil.generateSchemaJackson(SimpleV1.class);
        final String expected = """
            {"type":"object","id":"urn:jsonschema:com:mcherm:versionedserialization:objects:SimpleV1","properties":{"s":{"type":"string"},"i":{"type":"integer"}}}""";
        assertEquals(expected, schema);
    }

    @Test
    public void testGenerateSchemaVictools() {
        final String schema = SerializationUtil.generateSchemaVictools(SimpleV1.class);
        final String expected = """
            {"$schema":"https://json-schema.org/draft/2020-12/schema","type":"object","properties":{"i":{"type":"integer","x-javaType":"int"},"s":{"type":"string","x-javaType":"java.lang.String"}}}""";
        assertEquals(expected, schema);
    }
}
