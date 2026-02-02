package com.mcherm.versionedserialization;

import com.mcherm.versionedserialization.objects.MappedV1;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Tests of serialization with the MappedV1 class. */
public class MappedV1Test {

    @Test
    public void testRoundTrip() {
        final MappedV1 obj = new MappedV1();
        obj.name = "Alice";
        obj.metadata = new LinkedHashMap<>();
        obj.metadata.put("role", "admin");
        obj.metadata.put("dept", "engineering");

        final String serialized = SerializationUtil.serialize(obj);
        final String expected = """
            {"name":"Alice","metadata":{"role":"admin","dept":"engineering"}}""";
        assertEquals(expected, serialized);

        final MappedV1 deserialized = SerializationUtil.deserialize(serialized, MappedV1.class);
        assertEquals("Alice", deserialized.name);
        assertEquals(Map.of("role", "admin", "dept", "engineering"), deserialized.metadata);
    }

    @Test
    public void testGenerateSchemaJackson() {
        final String schema = SerializationUtil.generateSchemaJackson(MappedV1.class);
        // Note: Jackson's schema correctly represents the Map with additionalProperties.
        final String expected = """
            {"type":"object","id":"urn:jsonschema:com:mcherm:versionedserialization:objects:MappedV1",\
            "properties":{\
            "name":{"type":"string"},\
            "metadata":{"type":"object","additionalProperties":{"type":"string"}}\
            }}""";
        assertEquals(expected, schema);
    }

    @Test
    public void testGenerateSchemaVictools() {
        final String schema = SerializationUtil.generateSchemaVictools(MappedV1.class);
        // Note: Victools represents Map<String,String> as a $def with just {"type":"object"}.
        // The key and value type information is lost -- only the x-javaType annotation on the
        // property preserves the full generic type. This means schema diffing cannot detect
        // changes to a Map's value types.
        final String expected = """
                {"$schema":"https://json-schema.org/draft/2020-12/schema",\
                "$defs":{"Map(String,String)":{"type":"object"}},\
                "type":"object","properties":{\
                "metadata":{"$ref":"#/$defs/Map(String,String)","x-javaType":"java.util.Map<java.lang.String,java.lang.String>"},\
                "name":{"type":"string","x-javaType":"java.lang.String"}\
                }}""";
        assertEquals(expected, schema);
    }
}
