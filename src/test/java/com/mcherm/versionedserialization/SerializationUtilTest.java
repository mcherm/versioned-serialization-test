package com.mcherm.versionedserialization;

import com.mcherm.versionedserialization.objects.CustomSerializingV1;
import com.mcherm.versionedserialization.objects.SimpleV1;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

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
    public void testGenerateSchemaSimpleV1Jackson() {
        final String schema = SerializationUtil.generateSchemaJackson(SimpleV1.class);
        final String expected = """
            {"type":"object","id":"urn:jsonschema:com:mcherm:versionedserialization:objects:SimpleV1","properties":{"s":{"type":"string"},"i":{"type":"integer"}}}""";
        assertEquals(expected, schema);
    }

    @Test
    public void testGenerateSchemaSimpleV1Victools() {
        final String schema = SerializationUtil.generateSchemaVictools(SimpleV1.class);
        final String expected = """
            {"$schema":"https://json-schema.org/draft/2020-12/schema","type":"object","properties":{"i":{"type":"integer"},"s":{"type":"string"}}}""";
        assertEquals(expected, schema);
    }

    @Test
    public void testRoundTripCustomSerializingV1() {
        final CustomSerializingV1 obj = new CustomSerializingV1(
                "public_value",
                "getter_value",
                Optional.of("optional_value"),
                List.of("s1", "s2", "s3")
        );

        final String serialized = SerializationUtil.serialize(obj);
        final String expected = """
            {"simplePublic":"public_value","simpleGetter":"getter_value","optionalValue":"optional_value","listOfStrings":["s1","s2","s3"]}""";
        assertEquals(expected, serialized);

        final CustomSerializingV1 deserialized = SerializationUtil.deserialize(serialized, CustomSerializingV1.class);
        assertEquals("public_value", deserialized.simplePublic);
        assertEquals("getter_value", deserialized.getSimpleGetter());
        assertEquals(Optional.of("optional_value"), deserialized.getOptionalValue());
    }

    @Test
    public void testGenerateSchemaCustomSerializingV1Jackson() {
        final String schema = SerializationUtil.generateSchemaJackson(CustomSerializingV1.class);
        final String expected = """
            {"type":"object","id":"urn:jsonschema:com:mcherm:versionedserialization:objects:CustomSerializingV1","properties":{"simplePublic":{"type":"string"},"simpleGetter":{"type":"string"},"optionalValue":{"type":"string"},"listOfStrings":{"type":"array","items":{"type":"string"}}}}""";
        assertEquals(expected, schema);
    }

    @Test
    public void testGenerateSchemaCustomSerializingV1Victools() {
        final String schema = SerializationUtil.generateSchemaVictools(CustomSerializingV1.class);
        final String expected = """
            {"$schema":"https://json-schema.org/draft/2020-12/schema","type":"object","properties":{"listOfStrings":{"type":"array","items":{"type":"string"}},"optionalValue":{"type":["string","null"]},"simpleGetter":{"type":"string"},"simplePublic":{"type":"string"}}}""";
        assertEquals(expected, schema);
    }
}
