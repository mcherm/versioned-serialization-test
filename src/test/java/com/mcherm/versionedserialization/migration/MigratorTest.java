package com.mcherm.versionedserialization.migration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.mcherm.versionedserialization.SerializationUtil;
import com.mcherm.versionedserialization.objects.SimpleV1;
import com.mcherm.versionedserialization.objects.SimpleV2a;
import com.mcherm.versionedserialization.objects.SimpleV2b;
import com.mcherm.versionedserialization.objects.TypedV1;
import com.mcherm.versionedserialization.objects.TypedV2a;
import com.mcherm.versionedserialization.objects.TypedV2b;
import com.mcherm.versionedserialization.objects.TypedV2c;
import com.mcherm.versionedserialization.objects.MappedV1;
import com.mcherm.versionedserialization.objects.PolymorphicV1;
import com.mcherm.versionedserialization.objects.PolymorphicV2a;
import com.mcherm.versionedserialization.objects.RecordV1;
import com.mcherm.versionedserialization.objects.RecordV2a;
import com.mcherm.versionedserialization.objects.RecordV2b;
import com.mcherm.versionedserialization.objects.JsonPropV1;
import com.mcherm.versionedserialization.objects.JsonPropV2a;
import com.mcherm.versionedserialization.objects.JsonPropV2b;
import com.mcherm.versionedserialization.objects.RenamedV1;
import com.mcherm.versionedserialization.objects.RenamedV2a;
import com.mcherm.versionedserialization.objects.contents.Circle;
import com.mcherm.versionedserialization.objects.contents.Rectangle;
import com.mcherm.versionedserialization.schemadiff.SchemaParser;
import com.mcherm.versionedserialization.schemadiff.UnsupportedSchemaFeature;
import com.mcherm.versionedserialization.schemadiff.schema.SchemaInfo;
import org.junit.jupiter.api.Test;

import java.util.List;
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

    @Test
    public void testBasicArrayOfObjects() {
        record WidgetV1(String name, int shoeSize) {}
        record WidgetV2(String name, String soleShape) {} // added soleShape; removed shoeSize
        record ObjectV1(int shelfNumber, List<WidgetV1> widgets) {}
        record ObjectV2(int shelfNumber, List<WidgetV2> widgets) {} // added label
        final ObjectV1 sourceObject = new ObjectV1(46, List.of(new WidgetV1("Reebok", 6), new WidgetV1("Vans", 5)));
        final ObjectV2 expectedOutput = new ObjectV2(46, List.of(new WidgetV2("Reebok", ""), new WidgetV2("Vans", "")));
        final UpdateRules testUpdateRules = new UpdateRules(Map.of());
        // --- run test ---
        assertMigration(sourceObject, expectedOutput, testUpdateRules);
    }

    @Test
    public void testEmptyArray() {
        record WidgetV1(String name, int shoeSize) {}
        record WidgetV2(String name, String soleShape) {}
        record ObjectV1(int shelfNumber, List<WidgetV1> widgets) {}
        record ObjectV2(int shelfNumber, List<WidgetV2> widgets) {}
        final ObjectV1 sourceObject = new ObjectV1(46, List.of());
        final ObjectV2 expectedOutput = new ObjectV2(46, List.of());
        final UpdateRules testUpdateRules = new UpdateRules(Map.of());
        // --- run test ---
        assertMigration(sourceObject, expectedOutput, testUpdateRules);
    }

    @Test
    public void testSingleElementArray() {
        record WidgetV1(String name, int shoeSize) {}
        record WidgetV2(String name, String soleShape) {}
        record ObjectV1(int shelfNumber, List<WidgetV1> widgets) {}
        record ObjectV2(int shelfNumber, List<WidgetV2> widgets) {}
        final ObjectV1 sourceObject = new ObjectV1(46, List.of(new WidgetV1("Reebok", 6)));
        final ObjectV2 expectedOutput = new ObjectV2(46, List.of(new WidgetV2("Reebok", "")));
        final UpdateRules testUpdateRules = new UpdateRules(Map.of());
        // --- run test ---
        assertMigration(sourceObject, expectedOutput, testUpdateRules);
    }

    @Test
    public void testNestedArrays() {
        record ItemV1(String color) {}
        record ItemV2(String color, String size) {}
        record BoxV1(String label, List<ItemV1> items) {}
        record BoxV2(String label, List<ItemV2> items) {}
        record WarehouseV1(List<BoxV1> boxes) {}
        record WarehouseV2(List<BoxV2> boxes) {}
        final WarehouseV1 sourceObject = new WarehouseV1(List.of(
                new BoxV1("A", List.of(new ItemV1("red"), new ItemV1("blue"))),
                new BoxV1("B", List.of(new ItemV1("green")))
        ));
        final WarehouseV2 expectedOutput = new WarehouseV2(List.of(
                new BoxV2("A", List.of(new ItemV2("red", ""), new ItemV2("blue", ""))),
                new BoxV2("B", List.of(new ItemV2("green", "")))
        ));
        final UpdateRules testUpdateRules = new UpdateRules(Map.of());
        // --- run test ---
        assertMigration(sourceObject, expectedOutput, testUpdateRules);
    }


    // ===== Typed tests: various field type additions and changes =====

    @Test
    public void testTyped_V1ToV2a_addPrimitiveBoolean() {
        final TypedV1 source = new TypedV1();
        source.name = "test";
        source.count = 5;
        final TypedV2a expected = new TypedV2a();
        expected.name = "test";
        expected.count = 5;
        expected.active = false;
        final UpdateRules testUpdateRules = new UpdateRules(Map.of());
        // --- run test ---
        assertMigration(source, expected, testUpdateRules);
    }

    @Test
    public void testTyped_V1ToV2b_addListOfStrings() {
        final TypedV1 source = new TypedV1();
        source.name = "test";
        source.count = 5;
        final TypedV2b expected = new TypedV2b();
        expected.name = "test";
        expected.count = 5;
        expected.tags = List.of();
        final UpdateRules testUpdateRules = new UpdateRules(Map.of());
        // --- run test ---
        assertMigration(source, expected, testUpdateRules);
    }

    @Test
    public void testTyped_V1ToV2c_changeFieldType() {
        final TypedV1 source = new TypedV1();
        source.name = "test";
        source.count = 5;
        final TypedV2c expected = new TypedV2c();
        expected.name = "test";
        expected.count = "5";
        final UpdateRules testUpdateRules = new UpdateRules(Map.of(
                "count", (UpdateContext ctx, String field) ->
                        TextNode.valueOf(ctx.getSourceValue("count").map(JsonNode::asText).orElse(""))
        ));
        // --- run test ---
        assertMigration(source, expected, testUpdateRules);
    }

    // ===== Mapped tests: Map fields =====

    @Test
    public void testMapped_V1_migration() {
        final MappedV1 source = new MappedV1();
        source.name = "test";
        source.metadata = Map.of("key", "value");
        final UpdateRules testUpdateRules = new UpdateRules(Map.of());
        // --- run test ---
        assertMigration(source, source, testUpdateRules);
    }

    // ===== Renamed tests: field renaming with UpdateRule =====

    @Test
    public void testRenamed_V1ToV2a_withRule() {
        final RenamedV1 source = new RenamedV1();
        source.name = "Alice";
        source.value = 42;
        final RenamedV2a expected = new RenamedV2a();
        expected.fullName = "Alice";
        expected.value = 42;
        final UpdateRules testUpdateRules = new UpdateRules(Map.of(
                "fullName", (UpdateContext ctx, String field) ->
                        ctx.getSourceValue("name").orElse(TextNode.valueOf(""))
        ));
        // --- run test ---
        assertMigration(source, expected, testUpdateRules);
    }

    // ===== Polymorphic tests: Jackson @JsonTypeInfo =====

    // FIXME: Re-enable once SchemaParser supports anyOf/oneOf for polymorphic types
    @org.junit.jupiter.api.Disabled
    @Test
    public void testPolymorphic_V1ToV2a_addTopLevelField() {
        final PolymorphicV1 source = new PolymorphicV1();
        source.label = "art";
        final Circle c = new Circle();
        c.color = "red";
        c.radius = 5.0;
        final Rectangle r = new Rectangle();
        r.color = "blue";
        r.width = 3.0;
        r.height = 4.0;
        source.shapes = List.of(c, r);
        final PolymorphicV2a expected = new PolymorphicV2a();
        expected.label = "art";
        expected.shapes = List.of(c, r);
        expected.artist = "";
        final UpdateRules testUpdateRules = new UpdateRules(Map.of());
        // --- run test ---
        assertMigration(source, expected, testUpdateRules);
    }

    // ===== Record tests: Java records =====

    @Test
    public void testRecord_V1ToV2a_addField() {
        final RecordV1 source = new RecordV1("Alice", 95, List.of("math"));
        final RecordV2a expected = new RecordV2a("Alice", 95, List.of("math"), "");
        final UpdateRules testUpdateRules = new UpdateRules(Map.of());
        // --- run test ---
        assertMigration(source, expected, testUpdateRules);
    }

    @Test
    public void testRecord_V1ToV2b_removeField() {
        final RecordV1 source = new RecordV1("Alice", 95, List.of("math"));
        final RecordV2b expected = new RecordV2b("Alice", 95);
        final UpdateRules testUpdateRules = new UpdateRules(Map.of());
        // --- run test ---
        assertMigration(source, expected, testUpdateRules);
    }

    // ===== JsonProperty tests: @JsonProperty field renaming =====
    // The Victools schema uses Java field names (firstName) but the actual JSON document
    // uses @JsonProperty names (first_name). The Migrator operates on the JSON using
    // schema field names, so it will try to add/remove fields by the wrong names.

    @Test
    public void testJsonProp_V1ToV2a_addField() {
        final JsonPropV1 source = new JsonPropV1();
        source.firstName = "Alice";
        source.lastName = "Smith";
        source.age = 30;
        final JsonPropV2a expected = new JsonPropV2a();
        expected.firstName = "Alice";
        expected.lastName = "Smith";
        expected.age = 30;
        expected.phoneNumber = "";
        final UpdateRules testUpdateRules = new UpdateRules(Map.of());
        // --- run test ---
        assertMigration(source, expected, testUpdateRules);
    }

    @Test
    public void testJsonProp_V1ToV2b_removeField() {
        final JsonPropV1 source = new JsonPropV1();
        source.firstName = "Alice";
        source.lastName = "Smith";
        source.age = 30;
        final JsonPropV2b expected = new JsonPropV2b();
        expected.firstName = "Alice";
        expected.age = 30;
        final UpdateRules testUpdateRules = new UpdateRules(Map.of());
        // --- run test ---
        assertMigration(source, expected, testUpdateRules);
    }

    /** Easy way to declare tests in this file. */
    private void assertMigration(Object sourceObject, Object expectedOutput, UpdateRules updateRules) {
        try {
            final Class<?> sourceClass = sourceObject.getClass();
            final Class<?> targetClass = expectedOutput.getClass();
            final JsonNode sourceDocument = SerializationUtil.serializeAsNode(sourceObject);
            final JsonNode expectedTargetDocument = SerializationUtil.serializeAsNode(expectedOutput);
            final SchemaInfo sourceSchema = SchemaParser.parse(SerializationUtil.generateSchema(sourceClass));
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
