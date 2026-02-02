package com.mcherm.versionedserialization.migration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.mcherm.versionedserialization.SerializationUtil;
import com.mcherm.versionedserialization.objects.SimpleV1;
import com.mcherm.versionedserialization.objects.SimpleV2a;
import com.mcherm.versionedserialization.objects.SimpleV2b;
import com.mcherm.versionedserialization.schemadiff.SchemaParser;
import com.mcherm.versionedserialization.schemadiff.UnsupportedSchemaFeature;
import com.mcherm.versionedserialization.schemadiff.schema.SchemaInfo;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class MigratorTest {

    @Test
    public void testSimple_V1ToV2a_useDefault() {
        final SimpleV1 simpleV1 = new SimpleV1();
        simpleV1.s = "abc";
        simpleV1.i = 23;
        final SimpleV2a simpleV2A = new SimpleV2a();
        simpleV2A.s = "abc";
        simpleV2A.i = 23;
        simpleV2A.extra = "";
        final UpdateRules testUpdateRules = new UpdateRules(Map.of());
        // --- run test ---
        assertMigration(simpleV1, simpleV2A, testUpdateRules);
    }

    @Test
    public void testSimple_V1ToV2a_useRule() {
        final SimpleV1 simpleV1 = new SimpleV1();
        simpleV1.s = "abc";
        simpleV1.i = 23;
        final SimpleV2a simpleV2A = new SimpleV2a();
        simpleV2A.s = "abc";
        simpleV2A.i = 23;
        simpleV2A.extra = "more";
        final UpdateRules testUpdateRules = new UpdateRules(Map.of(
                "extra", (UpdateContext updateContext, String fieldToPopulate) -> TextNode.valueOf("more")
        ));
        // --- run test ---
        assertMigration(simpleV1, simpleV2A, testUpdateRules);
    }

    @Test
    public void testSimple_V1ToV2b() {
        final SimpleV1 simpleV1 = new SimpleV1();
        simpleV1.s = "abc";
        simpleV1.i = 23;
        final SimpleV2b simpleV2B = new SimpleV2b();
        simpleV2B.i = 23;
        final UpdateRules testUpdateRules = new UpdateRules(Map.of());
        // --- run test ---
        assertMigration(simpleV1, simpleV2B, testUpdateRules);
    }

    @Test
    public void testBasicInnerObjects() {
        record WidgetV1(String name, int shoeSize) {}
        record WidgetV2(String name, String soleShape) {} // added soleShape; removed shoeSize
        record ObjectV1(int shelfNumber, WidgetV1 widget) {}
        record ObjectV2(int shelfNumber, WidgetV2 widget, String label) {} // added label
        final ObjectV1 sourceObject = new ObjectV1(46, new WidgetV1("Reebok", 6));
        final ObjectV2 expectedOutput = new ObjectV2(46, new WidgetV2("Reebok", ""), "");
        final UpdateRules testUpdateRules = new UpdateRules(Map.of());
        // --- run test ---
        assertMigration(sourceObject, expectedOutput, testUpdateRules);
    }


    /** Easy way to declare tests in this file. */
    private void assertMigration(Object sourceObject, Object expectedOutput, UpdateRules updateRules) {
        try {
            final Class<?> sourceClass = sourceObject.getClass();
            final Class<?> targetClass = expectedOutput.getClass();
            final JsonNode sourceDocument = SerializationUtil.serializeAsNode(sourceObject);
            final JsonNode expectedTargetDocument = SerializationUtil.serializeAsNode(expectedOutput);
            final SchemaParser schemaParser = new SchemaParser();
            final SchemaInfo sourceSchema = schemaParser.parse(SerializationUtil.generateSchema(sourceClass));
            final SchemaInfo targetSchema = schemaParser.parse(SerializationUtil.generateSchema(targetClass));

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
