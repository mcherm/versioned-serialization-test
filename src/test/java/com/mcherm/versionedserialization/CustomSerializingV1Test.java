package com.mcherm.versionedserialization;

import com.mcherm.versionedserialization.objects.CustomSerializingV1;
import com.mcherm.versionedserialization.objects.contents.Color;
import com.mcherm.versionedserialization.objects.contents.Lamp;
import com.mcherm.versionedserialization.objects.contents.Widget;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Tests of serialization with the CustomSerializingV1 class. */
public class CustomSerializingV1Test {
    @Test
    public void testRoundTrip() {
        final CustomSerializingV1 obj = new CustomSerializingV1(
                "public_value",
                "getter_value",
                Optional.of("optional_value"),
                List.of("s1", "s2", "s3"),
                List.of(new Widget("a1", 3), new Widget("b2", 7)),
                List.of(new Lamp("square", 34.7f)),
                new Widget("c3", 8),
                Color.GREEN
        );

        final String serialized = SerializationUtil.serialize(obj);
        final String expected = """
            {\
            "simplePublic":"public_value",\
            "simpleGetter":"getter_value",\
            "optionalValue":"optional_value",\
            "listOfStrings":["s1","s2","s3"],\
            "listOfWidgets":[{"name":"a1","shoeSize":3},{"name":"b2","shoeSize":7}],\
            "listOfLamps":[{"shape":"square","lumens":34.7}],\
            "nestedWidget":{"name":"c3","shoeSize":8},\
            "backgroundColor":"GREEN"\
            }""";
        assertEquals(expected, serialized);

        final CustomSerializingV1 deserialized = SerializationUtil.deserialize(serialized, CustomSerializingV1.class);
        assertEquals("public_value", deserialized.simplePublic);
        assertEquals("getter_value", deserialized.getSimpleGetter());
        assertEquals(Optional.of("optional_value"), deserialized.getOptionalValue());
    }

    @Test
    public void testGenerateSchemaJackson() {
        final String schema = SerializationUtil.generateSchemaJackson(CustomSerializingV1.class);
        final String expected = """
            {"type":"object","id":"urn:jsonschema:com:mcherm:versionedserialization:objects:CustomSerializingV1",\
            "properties":{\
            "simplePublic":{"type":"string"},\
            "simpleGetter":{"type":"string"},\
            "optionalValue":{"type":"string"},\
            "listOfStrings":{"type":"array","items":{"type":"string"}},\
            "listOfWidgets":{"type":"array","items":{"type":"object","id":"urn:jsonschema:com:mcherm:versionedserialization:objects:contents:Widget","properties":{"name":{"type":"string"},"shoeSize":{"type":"integer"}}}},\
            "listOfLamps":{"type":"array","items":{"type":"object","id":"urn:jsonschema:com:mcherm:versionedserialization:objects:contents:Lamp","properties":{"shape":{"type":"string"},"lumens":{"type":"number"}}}},\
            "nestedWidget":{"type":"object","$ref":"urn:jsonschema:com:mcherm:versionedserialization:objects:contents:Widget"},\
            "backgroundColor":{"type":"string","enum":["RED","ORANGE","YELLOW","GREEN","BLUE","INDIGO","VIOLET"]}\
            }}""";
        assertEquals(expected, schema);
    }

    @Test
    public void testGenerateSchemaVictools() {
        final String schema = SerializationUtil.generateSchemaVictools(CustomSerializingV1.class);
        final String expected = """
                {"$schema":"https://json-schema.org/draft/2020-12/schema",\
                "$defs":{\
                "Color":{"type":"string","enum":["RED","ORANGE","YELLOW","GREEN","BLUE","INDIGO","VIOLET"]},\
                "Lamp":{"type":"object","properties":{"lumens":{"type":"number","x-javaType":"float"},"shape":{"type":"string","x-javaType":"java.lang.String"}}},\
                "Widget":{"type":"object","properties":{"name":{"type":"string","x-javaType":"java.lang.String"},"shoeSize":{"type":"integer","x-javaType":"int"}}}\
                },\
                "type":"object","properties":{\
                "backgroundColor":{"$ref":"#/$defs/Color","x-javaType":"com.mcherm.versionedserialization.objects.contents.Color"},\
                "listOfLamps":{"x-javaType":"java.util.List<com.mcherm.versionedserialization.objects.contents.Lamp>","type":"array","items":{"$ref":"#/$defs/Lamp","x-javaType":"java.util.List<com.mcherm.versionedserialization.objects.contents.Lamp>"}},\
                "listOfStrings":{"x-javaType":"java.util.List<java.lang.String>","type":"array","items":{"type":"string","x-javaType":"java.util.List<java.lang.String>"}},\
                "listOfWidgets":{"x-javaType":"java.util.List<com.mcherm.versionedserialization.objects.contents.Widget>","type":"array","items":{"$ref":"#/$defs/Widget","x-javaType":"java.util.List<com.mcherm.versionedserialization.objects.contents.Widget>"}},\
                "nestedWidget":{"$ref":"#/$defs/Widget","x-javaType":"com.mcherm.versionedserialization.objects.contents.Widget"},\
                "optionalValue":{"type":["string","null"],"x-javaType":"java.util.Optional<java.lang.String>"},\
                "simpleGetter":{"type":"string","x-javaType":"java.lang.String"},\
                "simplePublic":{"type":"string","x-javaType":"java.lang.String"}\
                }}""";
        assertEquals(expected, schema);
    }

}
