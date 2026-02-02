package com.mcherm.versionedserialization;

import com.fasterxml.jackson.databind.JsonNode;
import com.mcherm.versionedserialization.migration.Migrator;
import com.mcherm.versionedserialization.migration.UpdateRules;
import com.mcherm.versionedserialization.schemadiff.SchemaParser;
import com.mcherm.versionedserialization.schemadiff.UnsupportedSchemaFeature;
import com.mcherm.versionedserialization.schemadiff.schema.SchemaInfo;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Tests of the scenario where code has changed but the name hasn't.
 *
 * <p>To test these we're using code similar to that in MigratorTest, but
 * we manually generated the "source" version and the current code has only
 * the target version.
 */
public class CodeChangedSameNameTest {

    // Original version used to generate the sourceDocument and sourceSchema in the test
    //    private static class Widget {
    //        public final String name;
    //        public Widget(final String name) {
    //            this.name = name;
    //        }
    //    }

    // Updated version with changes.
    private static class Widget {
        public final String name;
        public final String flavor;
        public Widget(final String name, final String flavor) {
            this.name = name;
            this.flavor = flavor;
        }
    }

    // Object containing it
    private static class SampleChildObject {
        public final Widget myWidget;
        public SampleChildObject(Widget myWidget) {
            this.myWidget = myWidget;
        }
    }

    @Test
    public void testChangeChildObject() {
        final String sourceDocumentStr = """
                {"myWidget":{"name":"Fred"}}""";
        final String sourceSchemaStr = """
                {"$schema":"https://json-schema.org/draft/2020-12/schema",\
                "$defs":{\
                "Widget":{"type":["object"],"properties":{"name":{"type":["string"],"x-javaType":"java.lang.String"}}}\
                },\
                "type":"object",\
                "properties":{\
                "myWidget":{"type":["object"],"properties":{"name":{"type":["string"],"x-javaType":"java.lang.String"}},"x-javaType":"com.mcherm.versionedserialization.CodeChangedSameNameTest$Widget"}\
                }}""";
        final SampleChildObject expectedObject = new SampleChildObject(new Widget("Fred", ""));
        final UpdateRules testUpdateRules = new UpdateRules(Map.of());
        // --- run test ---
        assertMigration(sourceDocumentStr, sourceSchemaStr, expectedObject, testUpdateRules);
    }


    // Object containing list of it
    private static class SampleListObject {
        public final List<Widget> myWidgets;
        public SampleListObject(List<Widget> myWidgets) {
            this.myWidgets = myWidgets;
        }
    }

    @Test
    public void testChangeListItem() {
        final String sourceDocumentStr = """
                {"myWidgets":[{"name":"Fred"},{"name":"Lucy"}]}""";
        final String sourceSchemaStr = """
                {"$schema":"https://json-schema.org/draft/2020-12/schema",\
                "$defs":{\
                "Widget":{"type":"object","properties":{"name":{"type":"string","x-javaType":"java.lang.String"}}}},\
                "type":"object",\
                "properties":{\
                "myWidgets":{"x-javaType":"java.util.List<com.mcherm.versionedserialization.CodeChangedSameNameTest$Widget>","type":"array","items":{"$ref":"#/$defs/Widget","x-javaType":"java.util.List<com.mcherm.versionedserialization.CodeChangedSameNameTest$Widget>"}}\
                }}""";
        final SampleListObject expectedObject =
                new SampleListObject(List.of(new Widget("Fred", ""), new Widget("Lucy", "")));
        final UpdateRules testUpdateRules = new UpdateRules(Map.of());
        // --- run test ---
        assertMigration(sourceDocumentStr, sourceSchemaStr, expectedObject, testUpdateRules);
    }


    /** Easy way to declare tests in this file. */
    private void assertMigration(String sourceDocumentStr, String sourceSchemaStr, Object expectedOutput, UpdateRules updateRules) {
        try {
            final Class<?> targetClass = expectedOutput.getClass();
            final JsonNode sourceDocument = SerializationUtil.deserializeAsNode(sourceDocumentStr);
            final JsonNode expectedTargetDocument = SerializationUtil.serializeAsNode(expectedOutput);
            final SchemaInfo sourceSchema = SchemaParser.parse(sourceSchemaStr);
            final SchemaInfo targetSchema = SchemaParser.parse(SerializationUtil.generateSchema(targetClass));

            // --- perform the work ---
            final Migrator migrator = new Migrator();
            final JsonNode targetDocument = migrator.migrate(sourceSchema, targetSchema, sourceDocument, updateRules);

            // --- compare the output ---
            assertEquals(expectedTargetDocument, targetDocument);
        } catch (final UnsupportedSchemaFeature err) {
            err.printStackTrace();
            fail();
        }
    }

}
