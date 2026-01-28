package com.mcherm.versionedserialization;

import com.mcherm.versionedserialization.objects.MoneyStuffV1;
import com.mcherm.versionedserialization.objects.NestingV1;
import com.mcherm.versionedserialization.objects.contents.CAD;
import com.mcherm.versionedserialization.objects.contents.Money;
import com.mcherm.versionedserialization.objects.contents.SelfNested;
import com.mcherm.versionedserialization.objects.contents.USD;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NestingV1Test {
    @Test
    public void testRoundTrip() {
        final NestingV1 obj = new NestingV1(
                new SelfNested(6, new SelfNested(8, new SelfNested(12, null)))
        );

        final String serialized = SerializationUtil.serialize(obj);
        final String expected = """
            {\
            "nestlings":{"value":6,"next":{"value":8,"next":{"value":12,"next":null}}}\
            }""";
        assertEquals(expected, serialized);

        final NestingV1 deserialized = SerializationUtil.deserialize(serialized, NestingV1.class);
        assertEquals(6, deserialized.getNestlings().getValue());
        assertEquals(8, deserialized.getNestlings().getNext().getValue());
        assertEquals(12, deserialized.getNestlings().getNext().getNext().getValue());
    }

    @Test
    public void testGenerateSchemaJackson() {
        final String schema = SerializationUtil.generateSchemaJackson(NestingV1.class);
        final String expected = """
            {"type":"object",\
            "id":"urn:jsonschema:com:mcherm:versionedserialization:objects:NestingV1",\
            "properties":{\
            "nestlings":{"type":"object","id":"urn:jsonschema:com:mcherm:versionedserialization:objects:contents:SelfNested","properties":{"value":{"type":"integer"},"next":{"type":"object","$ref":"urn:jsonschema:com:mcherm:versionedserialization:objects:contents:SelfNested"}}}\
            }}""";
        assertEquals(expected, schema);
    }

    @Test
    public void testGenerateSchemaVictools() {
        final String schema = SerializationUtil.generateSchemaVictools(NestingV1.class);
        final String expected = """
            {"$schema":"https://json-schema.org/draft/2020-12/schema",\
            "$defs":{\
            "SelfNested":{"type":"object","properties":{"next":{"$ref":"#/$defs/SelfNested"},"value":{"type":"integer"}}}\
            },\
            "type":"object",\
            "properties":{\
            "nestlings":{"$ref":"#/$defs/SelfNested"}\
            }}""";
        assertEquals(expected, schema);
    }
}
