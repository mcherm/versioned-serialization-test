package com.mcherm.versionedserialization.schemadiff;

import com.mcherm.versionedserialization.EvolutionTest;
import com.mcherm.versionedserialization.SerializationUtil;
import com.mcherm.versionedserialization.objects.CustomSerializingV1;
import com.mcherm.versionedserialization.objects.SimpleV1;
import com.mcherm.versionedserialization.schemadiff.deltas.Add;
import com.mcherm.versionedserialization.schemadiff.deltas.Change;
import com.mcherm.versionedserialization.schemadiff.deltas.Drop;
import com.mcherm.versionedserialization.schemadiff.deltas.SchemaDeltas;
import com.mcherm.versionedserialization.schemadiff.schema.SchemaInfo;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/** Tests of SchemaDiffer. */
public class SchemaDifferTest {

    @Test
    public void testSchemaDiffer() {
        assertSchemaDeltas(
                SimpleV1.class, EvolutionTest.SimpleV1AddOptionalField.class,
                List.of("extra"),
                List.of(),
                List.of()
        );
    }

    @Test
    public void testCustomSerializingUnchanged() {
        assertSchemaDeltas(
                CustomSerializingV1.class, CustomSerializingV1.class,
                List.of(),
                List.of(),
                List.of()
        );
    }

    public static class SimpleV1MinusS {
        public int i;
    }

    @Test
    public void testRemoveField() {
        assertSchemaDeltas(
                SimpleV1.class, SimpleV1MinusS.class,
                List.of(),
                List.of("s"),
                List.of()
        );
    }

    public static class SimpleV1ChangeS {
        public int s;
        public int i;
    }

    @Test
    public void testChangeField() {
        assertSchemaDeltas(
                SimpleV1.class, SimpleV1ChangeS.class,
                List.of(),
                List.of(),
                List.of("s")
        );
    }

    public static class Foo {
        public String s;
    }
    public static class Bar {
        public String s;
    }

    @Test
    public void testDifferentClassNoChanges() {
        assertSchemaDeltas(
                Foo.class, Bar.class,
                List.of(),
                List.of(),
                List.of()
        );
    }

    public static class InnerV1 {
        public String s;
    }
    public static class InnerV2 {
        public String s;
        public String t;
    }
    public static class OuterV1 {
        public String a;
        public InnerV1 inner;
    }
    public static class OuterV2 {
        public String a;
        public InnerV2 inner;
    }

    @Test
    public void testChangeToNestedClass() {
        assertSchemaDeltas(
                OuterV1.class, OuterV2.class,
                List.of("inner/t"),
                List.of(),
                List.of()
        );
    }


    public static class WidgetV1 {
        public String s;
    }
    public static class WidgetV2 {
        public String s;
        public String t;
    }
    // FIXME: Note: I am naming this with a "Z" because until back-references work I need to hack the alphabetical order
    public static class ZHasListOfWidgetsV1 {
        public List<WidgetV1> widgets;
    }
    public static class ZHasListOfWidgetsV2 {
        public List<WidgetV2> widgets;
    }

    @Test
    public void testChangeFieldInList() {
        assertSchemaDeltas(
                ZHasListOfWidgetsV1.class, ZHasListOfWidgetsV2.class,
                List.of("widgets[]t"),
                List.of(),
                List.of()
        );
    }

    public static class DoublyNestedV1 {
        public ZHasListOfWidgetsV1 inner;
    }
    public static class DoublyNestedV2 {
        public ZHasListOfWidgetsV2 inner;
    }

    @Test
    public void testDoublyNested() {
        assertSchemaDeltas(
                DoublyNestedV1.class, DoublyNestedV2.class,
                List.of("inner/widgets[]t"),
                List.of(),
                List.of()
        );
    }

    /** Easy way to declare tests in this file. */
    private void assertSchemaDeltas(Class<?> first, Class<?> second, List<String> adds, List<String> drops, List<String> changes) {
        try {
            final SchemaParser parser = new SchemaParser();
            final SchemaInfo v1Schema = parser.parse(SerializationUtil.generateSchema(first));
            final SchemaInfo v2Schema = parser.parse(SerializationUtil.generateSchema(second));
            final SchemaDeltas schemaDeltas = SchemaDiffer.diff(v1Schema, v2Schema);
            assertEquals(adds, schemaDeltas.getAdds().stream().map(Add::getFieldName).toList());
            assertEquals(drops, schemaDeltas.getDrops().stream().map(Drop::getFieldName).toList());
            assertEquals(changes, schemaDeltas.getChanges().stream().map(Change::getFieldName).toList());
        } catch (final UnsupportedSchemaFeature err) {
            err.printStackTrace();
            fail();
        }
    }
}
