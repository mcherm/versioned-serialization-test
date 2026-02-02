package com.mcherm.versionedserialization;

import com.mcherm.versionedserialization.objects.PolymorphicV1;
import com.mcherm.versionedserialization.objects.contents.Circle;
import com.mcherm.versionedserialization.objects.contents.Rectangle;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/**
 * Tests of serialization with the PolymorphicV1 class.
 * <p>
 * This class contains a {@code List<Shape>} where Shape uses Jackson's
 * {@code @JsonTypeInfo} and {@code @JsonSubTypes} for polymorphic serialization.
 * The serialized JSON correctly includes a "type" discriminator and subtype-specific
 * fields, but the Victools schema only captures the base class properties -- the
 * discriminator and subtype fields are invisible to the schema. This means
 * schema-based diffing cannot detect changes to subtype-specific fields.
 */
public class PolymorphicV1Test {

    @Test
    public void testRoundTrip() {
        final PolymorphicV1 obj = new PolymorphicV1();
        obj.label = "art";
        final Circle c = new Circle();
        c.color = "red";
        c.radius = 5.0;
        final Rectangle r = new Rectangle();
        r.color = "blue";
        r.width = 3.0;
        r.height = 4.0;
        obj.shapes = List.of(c, r);

        final String serialized = SerializationUtil.serialize(obj);
        // Note: the serialized JSON includes "type" discriminator and subtype-specific
        // fields like "radius", "width", "height" that are NOT in the Victools schema.
        final String expected = """
            {\
            "label":"art",\
            "shapes":[\
            {"type":"circle","color":"red","radius":5.0},\
            {"type":"rectangle","color":"blue","width":3.0,"height":4.0}\
            ]\
            }""";
        assertEquals(expected, serialized);

        final PolymorphicV1 deserialized = SerializationUtil.deserialize(serialized, PolymorphicV1.class);
        assertEquals("art", deserialized.label);
        assertEquals(2, deserialized.shapes.size());
        assertInstanceOf(Circle.class, deserialized.shapes.get(0));
        assertEquals(5.0, ((Circle) deserialized.shapes.get(0)).radius);
        assertInstanceOf(Rectangle.class, deserialized.shapes.get(1));
        assertEquals(3.0, ((Rectangle) deserialized.shapes.get(1)).width);
    }

    @Test
    public void testGenerateSchemaJackson() {
        final String schema = SerializationUtil.generateSchemaJackson(PolymorphicV1.class);
        final String expected = """
            {"type":"object",\
            "id":"urn:jsonschema:com:mcherm:versionedserialization:objects:PolymorphicV1",\
            "properties":{\
            "label":{"type":"string"},\
            "shapes":{"type":"array","items":{"type":"object",\
            "id":"urn:jsonschema:com:mcherm:versionedserialization:objects:contents:Shape",\
            "properties":{"color":{"type":"string"}}}}\
            }}""";
        assertEquals(expected, schema);
    }

    @Test
    public void testGenerateSchemaVictools() {
        final String schema = SerializationUtil.generateSchemaVictools(PolymorphicV1.class);
        // With JacksonModule registered, Victools now generates full polymorphic schema with
        // anyOf for subtypes, discriminator "type" field, and subtype-specific properties.
        final String expected = """
                {"$schema":"https://json-schema.org/draft/2020-12/schema",\
                "$defs":{\
                "Circle-1":{"type":"object","properties":{"color":{"type":"string","x-javaType":"java.lang.String"},"radius":{"type":"number","x-javaType":"double"}}},\
                "Circle-2":{"$ref":"#/$defs/Circle-1","type":"object","properties":{"type":{"const":"circle"}},"required":["type"]},\
                "Rectangle-1":{"type":"object","properties":{"color":{"type":"string","x-javaType":"java.lang.String"},"height":{"type":"number","x-javaType":"double"},"width":{"type":"number","x-javaType":"double"}}},\
                "Rectangle-2":{"$ref":"#/$defs/Rectangle-1","type":"object","properties":{"type":{"const":"rectangle"}},"required":["type"]}},\
                "type":"object","properties":{\
                "label":{"type":"string","x-javaType":"java.lang.String"},\
                "shapes":{"x-javaType":"java.util.List<com.mcherm.versionedserialization.objects.contents.Shape>","type":"array","items":{"anyOf":[{"$ref":"#/$defs/Circle-2","x-javaType":"java.util.List<com.mcherm.versionedserialization.objects.contents.Shape>"},{"$ref":"#/$defs/Rectangle-2","x-javaType":"java.util.List<com.mcherm.versionedserialization.objects.contents.Shape>"}]}}\
                }}""";
        assertEquals(expected, schema);
    }
}
