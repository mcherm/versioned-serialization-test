package com.mcherm.versionedserialization.schemadiff;

import com.fasterxml.jackson.databind.node.DoubleNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Tests of DefaultableClasses. */
public class DefaultableClassesTest {

    @Test
    public void testString() {
        assertEquals(Optional.of(TextNode.valueOf("")), DefaultableClasses.getDefault(String.class));
    }

    @Test
    public void testInt() {
        assertEquals(Optional.of(IntNode.valueOf(0)), DefaultableClasses.getDefault(Integer.class));
    }

    @Test
    public void testDouble() {
        assertEquals(Optional.of(DoubleNode.valueOf(0.0)), DefaultableClasses.getDefault(Double.class));
    }

    @Test
    public void testOptional() {
        final Optional<String> os = Optional.of("abc");
        assertEquals(Optional.of(NullNode.getInstance()), DefaultableClasses.getDefault(os.getClass()));
        final Optional<String> os2 = Optional.empty();
        assertEquals(Optional.of(NullNode.getInstance()), DefaultableClasses.getDefault(os2.getClass()));
        final Optional<List<Integer>> oList = Optional.of(List.of(1, 2, 3));
        assertEquals(Optional.of(NullNode.getInstance()), DefaultableClasses.getDefault(oList.getClass()));
    }

    @Test
    public void testList() {
        final List<String> os = List.of("abc", "def", "ghi");
        assertEquals(Optional.of(JsonNodeFactory.instance.arrayNode()), DefaultableClasses.getDefault(os.getClass()));
        final List<String> os2 = List.of();
        assertEquals(Optional.of(JsonNodeFactory.instance.arrayNode()), DefaultableClasses.getDefault(os2.getClass()));
        final List<Integer> oList = List.of(1, 2, 3);
        assertEquals(Optional.of(JsonNodeFactory.instance.arrayNode()), DefaultableClasses.getDefault(oList.getClass()));
    }

}
