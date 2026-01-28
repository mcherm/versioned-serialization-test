package com.mcherm.versionedserialization;

import com.mcherm.versionedserialization.objects.SimpleV1;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SerializationUtilTest {

    @Test
    public void testRoundTripSimpleV1() {
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
    public void testGenerateSchemaSimpleV1() {
        final String schema = SerializationUtil.generateSchema(SimpleV1.class);
        final String expected = """
            {"type":"object","id":"urn:jsonschema:com:mcherm:versionedserialization:objects:SimpleV1","properties":{"s":{"type":"string"},"i":{"type":"integer"}}}""";
        assertEquals(expected, schema);
    }
}
