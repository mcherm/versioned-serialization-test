package com.mcherm.versionedserialization.schemadiff;

import com.mcherm.versionedserialization.schemadiff.schema.SchemaInfo;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

/** Tests of SchemaParser. */
public class SchemaParserTest {

    @Test
    public void testParseSimpleV1() {
        final String schema = """
            {"$schema":"https://json-schema.org/draft/2020-12/schema",\
            "type":"object",\
            "properties":{"i":{"type":"integer"},"s":{"type":"string"}}\
            }""";

        final SchemaParser parser = new SchemaParser();
        try {
            final SchemaInfo schemaInfo = parser.parse(schema);
            final String expected = """
                    {"$schema":"https://json-schema.org/draft/2020-12/schema",\
                    "$defs":{},\
                    "type":"object",\
                    "properties":{"i":{"type":["integer"]},"s":{"type":["string"]}}}""";
            assertEquals(expected, schemaInfo.toString());
        } catch (UnsupportedSchemaFeature err) {
            err.printStackTrace();
            fail(err.getMessage());
        }
    }

    @Test
    public void testParseCustomSerializingV1() {
        final String schema = """
                {"$schema":"https://json-schema.org/draft/2020-12/schema",\
                "$defs":{\
                "Color":{"type":"string","enum":["RED","ORANGE","YELLOW","GREEN","BLUE","INDIGO","VIOLET"]},\
                "Lamp":{"type":"object","properties":{"lumens":{"type":"number"},"shape":{"type":"string"}}},\
                "Widget":{"type":"object","properties":{"name":{"type":"string"},"shoeSize":{"type":"integer"}}}\
                },\
                "type":"object","properties":{\
                "backgroundColor":{"$ref":"#/$defs/Color"},\
                "listOfLamps":{"type":"array","items":{"$ref":"#/$defs/Lamp"}},\
                "listOfStrings":{"type":"array","items":{"type":"string"}},\
                "listOfWidgets":{"type":"array","items":{"$ref":"#/$defs/Widget"}},\
                "nestedWidget":{"$ref":"#/$defs/Widget"},\
                "optionalValue":{"type":["string","null"]},\
                "simpleGetter":{"type":"string"},\
                "simplePublic":{"type":"string"}\
                }}""";

        final SchemaParser parser = new SchemaParser();
        try {
            final SchemaInfo schemaInfo = parser.parse(schema);
            final String expected = """
                    {"$schema":"https://json-schema.org/draft/2020-12/schema",\
                    "$defs":{\
                    "Color":{"type":["string"],"enum":["RED","ORANGE","YELLOW","GREEN","BLUE","INDIGO","VIOLET"]},\
                    "Lamp":{"type":["object"],"properties":{"lumens":{"type":["number"]},"shape":{"type":["string"]}}},\
                    "Widget":{"type":["object"],"properties":{"name":{"type":["string"]},"shoeSize":{"type":["integer"]}}}},\
                    "type":"object",\
                    "properties":{\
                    "backgroundColor":{"type":["string"],"enum":["RED","ORANGE","YELLOW","GREEN","BLUE","INDIGO","VIOLET"]},\
                    "listOfLamps":{"type":["array"],"items":{"type":["object"],"properties":{"lumens":{"type":["number"]},"shape":{"type":["string"]}}}},\
                    "listOfStrings":{"type":["array"],"items":{"type":["string"]}},\
                    "listOfWidgets":{"type":["array"],"items":{"type":["object"],"properties":{"name":{"type":["string"]},"shoeSize":{"type":["integer"]}}}},\
                    "nestedWidget":{"type":["object"],"properties":{"name":{"type":["string"]},"shoeSize":{"type":["integer"]}}},\
                    "optionalValue":{"type":["string","null"]},\
                    "simpleGetter":{"type":["string"]},\
                    "simplePublic":{"type":["string"]}\
                    }}""";
            assertEquals(expected, schemaInfo.toString());
        } catch (UnsupportedSchemaFeature err) {
            err.printStackTrace();
            fail(err.getMessage());
        }
    }


    @Test
    public void testParseMoneyStuffV1() {
        final String schema = """
            {"$schema":"https://json-schema.org/draft/2020-12/schema",\
            "$defs":{\
            "Money(CAD)":{"type":"object","properties":{"amount":{"type":"number"},"currency":{"type":"string"}}},\
            "Money(USD)":{"type":"object","properties":{"amount":{"type":"number"},"currency":{"type":"string"}}}\
            },\
            "type":"object","properties":{\
            "balanceCa":{"$ref":"#/$defs/Money(CAD)"},\
            "balanceUs":{"$ref":"#/$defs/Money(USD)"},\
            "debtUs":{"$ref":"#/$defs/Money(USD)"},\
            "mainCurrency":{"type":"string"}\
            }}""";

        final SchemaParser parser = new SchemaParser();
        try {
            final SchemaInfo schemaInfo = parser.parse(schema);
            final String expected = """
                    {"$schema":"https://json-schema.org/draft/2020-12/schema",\
                    "$defs":{\
                    "Money(CAD)":{"type":["object"],"properties":{"amount":{"type":["number"]},"currency":{"type":["string"]}}},\
                    "Money(USD)":{"type":["object"],"properties":{"amount":{"type":["number"]},"currency":{"type":["string"]}}}\
                    },\
                    "type":"object",\
                    "properties":{\
                    "balanceCa":{"type":["object"],"properties":{"amount":{"type":["number"]},\
                    "currency":{"type":["string"]}}},\
                    "balanceUs":{"type":["object"],"properties":{"amount":{"type":["number"]},"currency":{"type":["string"]}}},\
                    "debtUs":{"type":["object"],"properties":{"amount":{"type":["number"]},"currency":{"type":["string"]}}},\
                    "mainCurrency":{"type":["string"]}\
                    }}""";
            assertEquals(expected, schemaInfo.toString());
        } catch (UnsupportedSchemaFeature err) {
            err.printStackTrace();
            fail(err.getMessage());
        }
    }

    @Test
    public void testParseNestingV1() {
        final String schema = """
            {"$schema":"https://json-schema.org/draft/2020-12/schema",\
            "$defs":{\
            "SelfNested":{"type":"object","properties":{"next":{"$ref":"#/$defs/SelfNested"},"value":{"type":"integer"}}}\
            },\
            "type":"object",\
            "properties":{\
            "nestlings":{"$ref":"#/$defs/SelfNested"}\
            }}""";

        final SchemaParser parser = new SchemaParser();
        try {
            final SchemaInfo schemaInfo = parser.parse(schema);
            final String expected = """
                {"$schema":"https://json-schema.org/draft/2020-12/schema",\
                "$defs":{\
                "SelfNested":{*"type":["object"],"properties":{"next":{"$selfRef":"SelfNested"},"value":{"type":["integer"]}}}\
                },\
                "type":"object",\
                "properties":{\
                "nestlings":{*"type":["object"],"properties":{"next":{"$selfRef":"SelfNested"},"value":{"type":["integer"]}}}\
                }}""";
            assertEquals(expected, schemaInfo.toString());
        } catch (UnsupportedSchemaFeature err) {
            err.printStackTrace();
            fail(err.getMessage());
        }
    }

    @Test
    public void testParseWithOneStepNesting() {
        final String schema = """
            {"$schema":"https://json-schema.org/draft/2020-12/schema",\
            "$defs":{\
            "A":{"type":"object","properties":{"link1":{"$ref":"#/$defs/A"}}}\
            },\
            "type":"object",\
            "properties":{\
            "contents":{"$ref":"#/$defs/A"}\
            }}""";

        final SchemaParser parser = new SchemaParser();
        try {
            final SchemaInfo schemaInfo = parser.parse(schema);
            final String expected = """
                {"$schema":"https://json-schema.org/draft/2020-12/schema",\
                "$defs":{\
                "A":{*"type":["object"],"properties":{"link1":{"$selfRef":"A"}}}\
                },\
                "type":"object",\
                "properties":{\
                "contents":{*"type":["object"],"properties":{"link1":{"$selfRef":"A"}}}\
                }}""";
            assertEquals(expected, schemaInfo.toString());
        } catch (UnsupportedSchemaFeature err) {
            err.printStackTrace();
            fail(err.getMessage());
        }
    }

    @Test
    public void testParseWithTwoStepNesting() {
        final String schema = """
            {"$schema":"https://json-schema.org/draft/2020-12/schema",\
            "$defs":{\
            "A":{"type":"object","properties":{"link1":{"$ref":"#/$defs/B"}}},\
            "B":{"type":"object","properties":{"link2":{"$ref":"#/$defs/A"}}}\
            },\
            "type":"object",\
            "properties":{\
            "contents":{"$ref":"#/$defs/A"}\
            }}""";

        final SchemaParser parser = new SchemaParser();
        try {
            final SchemaInfo schemaInfo = parser.parse(schema);
            final String expected = """
                {"$schema":"https://json-schema.org/draft/2020-12/schema",\
                "$defs":{\
                "A":{"type":["object"],"properties":{"link1":{*"type":["object"],"properties":{"link2":{*"type":["object"],"properties":{"link1":{"$selfRef":"B"}}}}}}},\
                "B":{*"type":["object"],"properties":{"link2":{*"type":["object"],"properties":{"link1":{"$selfRef":"B"}}}}}\
                },\
                "type":"object",\
                "properties":{\
                "contents":{"type":["object"],"properties":{"link1":{*"type":["object"],"properties":{"link2":{*"type":["object"],"properties":{"link1":{"$selfRef":"B"}}}}}}}\
                }}""";
            assertEquals(expected, schemaInfo.toString());
        } catch (UnsupportedSchemaFeature err) {
            err.printStackTrace();
            fail(err.getMessage());
        }
    }

    @Test
    public void testParseWithNesting() {
        final String schema = """
            {"$schema":"https://json-schema.org/draft/2020-12/schema",\
            "$defs":{\
            "A":{"type":"object","properties":{"first":{"$ref":"#/$defs/B"},"second":{"$ref":"#/$defs/C"}}},\
            "B":{"type":"object","properties":{"only":{"$ref":"#/$defs/D"},"data":{"type":"string"}}},\
            "C":{"type":"object","properties":{"data":{"type":"string"}}},\
            "D":{"type":"object","properties":{"back":{"$ref":"#/$defs/B"},"data":{"type":"string"}}}\
            },\
            "type":"object",\
            "properties":{\
            "contents":{"$ref":"#/$defs/A"}\
            }}""";

        final SchemaParser parser = new SchemaParser();
        try {
            final SchemaInfo schemaInfo = parser.parse(schema);
            final String expected = """
                    {"$schema":"https://json-schema.org/draft/2020-12/schema",\
                    "$defs":{\
                    "A":{"type":["object"],"properties":{"first":{"type":["object"],"properties":{"only":{*"type":["object"],"properties":{"back":{*"type":["object"],"properties":{"only":{"$selfRef":"D"},"data":{"type":["string"]}}},"data":{"type":["string"]}}},"data":{"type":["string"]}}},"second":{"type":["object"],"properties":{"data":{"type":["string"]}}}}},\
                    "B":{"type":["object"],"properties":{"only":{*"type":["object"],"properties":{"back":{*"type":["object"],"properties":{"only":{"$selfRef":"D"},"data":{"type":["string"]}}},"data":{"type":["string"]}}},"data":{"type":["string"]}}},\
                    "C":{"type":["object"],"properties":{"data":{"type":["string"]}}},\
                    "D":{*"type":["object"],"properties":{"back":{*"type":["object"],"properties":{"only":{"$selfRef":"D"},"data":{"type":["string"]}}},"data":{"type":["string"]}}}\
                    },\
                    "type":"object",\
                    "properties":{\
                    "contents":{"type":["object"],"properties":{"first":{"type":["object"],"properties":{"only":{*"type":["object"],"properties":{"back":{*"type":["object"],"properties":{"only":{"$selfRef":"D"},"data":{"type":["string"]}}},"data":{"type":["string"]}}},"data":{"type":["string"]}}},"second":{"type":["object"],"properties":{"data":{"type":["string"]}}}}}\
                    }}""";
            assertEquals(expected, schemaInfo.toString());
        } catch (UnsupportedSchemaFeature err) {
            err.printStackTrace();
            fail(err.getMessage());
        }
    }

}
