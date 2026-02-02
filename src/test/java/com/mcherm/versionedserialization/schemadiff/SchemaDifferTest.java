package com.mcherm.versionedserialization.schemadiff;

import com.mcherm.versionedserialization.SerializationUtil;
import com.mcherm.versionedserialization.objects.CustomSerializingV1;
import com.mcherm.versionedserialization.objects.SimpleV1;
import com.mcherm.versionedserialization.objects.SimpleV2a;
import com.mcherm.versionedserialization.objects.SimpleV2b;
import com.mcherm.versionedserialization.objects.SimpleV2c;
import com.mcherm.versionedserialization.schemadiff.deltas.SchemaDeltas;
import com.mcherm.versionedserialization.schemadiff.schema.SchemaInfo;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/** Tests of SchemaDiffer. */
public class SchemaDifferTest {

    record Expect(String field, String effect) {}

    @Test
    public void testSimple_V1V2a_addFieldThatDefaults() {
        assertSchemaDeltas(
                SimpleV1.class, SimpleV2a.class,
                Set.of(
                        new Expect("extra","DefaultingAdd")
                )
        );
    }

    @Test
    public void testSimple_V1V2b_RemoveField() {
        assertSchemaDeltas(
                SimpleV1.class, SimpleV2b.class,
                Set.of(
                        new Expect("s","Drop")
                )
        );
    }

    @Test
    public void testSimple_V1V2c_noChanges() {
        assertSchemaDeltas(
                SimpleV1.class, SimpleV2c.class,
                Set.of()
        );
    }

    @Test
    public void testCustomSerializingUnchanged() {
        assertSchemaDeltas(
                CustomSerializingV1.class, CustomSerializingV1.class,
                Set.of()
        );
    }

    public static class SimpleV1PlusDate {
        public String s;
        public int i;
        public LocalDate date;
    }

    @Test
    public void testAddDate() {
        assertSchemaDeltas(
                SimpleV1.class, SimpleV1PlusDate.class,
                Set.of(new Expect("date", "CustomAdd"))
        );
    }

    public static class SimpleV1MinusS {
        public int i;
    }

    @Test
    public void testRemoveField() {
        assertSchemaDeltas(
                SimpleV1.class, SimpleV1MinusS.class,
                Set.of(new Expect("s", "Drop"))
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
                Set.of(new Expect("s", "Change"))
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
                Set.of()
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
                Set.of(new Expect("inner/t", "DefaultingAdd"))
        );
    }


    public static class WidgetV1 {
        public String s;
    }
    public static class WidgetV2 {
        public String s;
        public String t;
    }
    public static class HasListOfWidgetsV1 {
        public List<WidgetV1> widgets;
    }
    public static class HasListOfWidgetsV2 {
        public List<WidgetV2> widgets;
    }

    @Test
    public void testChangeFieldInList() {
        assertSchemaDeltas(
                HasListOfWidgetsV1.class, HasListOfWidgetsV2.class,
                Set.of(new Expect("widgets[]t", "DefaultingAdd"))
        );
    }

    public static class DoublyNestedV1 {
        public HasListOfWidgetsV1 inner;
    }
    public static class DoublyNestedV2 {
        public HasListOfWidgetsV2 inner;
    }

    @Test
    public void testDoublyNested() {
        assertSchemaDeltas(
                DoublyNestedV1.class, DoublyNestedV2.class,
                Set.of(new Expect("inner/widgets[]t", "DefaultingAdd"))
        );
    }

    /** Easy way to declare tests in this file. */
    private void assertSchemaDeltas(Class<?> first, Class<?> second, Set<Expect> expected) {
        try {
            final SchemaInfo v1Schema = SchemaParser.parse(SerializationUtil.generateSchema(first));
            final SchemaInfo v2Schema = SchemaParser.parse(SerializationUtil.generateSchema(second));
            final SchemaDeltas schemaDeltas = SchemaDiffer.diff(v1Schema, v2Schema);
            assertEquals(
                    expected,
                    schemaDeltas.getDeltas().stream()
                            .map(x -> new Expect(x.getFieldName(), x.getClass().getSimpleName()))
                            .collect(Collectors.toSet())
            );
        } catch (final UnsupportedSchemaFeature err) {
            err.printStackTrace();
            fail();
        }
    }
}
