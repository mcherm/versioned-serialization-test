package com.mcherm.versionedserialization;

import com.fasterxml.jackson.databind.JsonNode;
import com.mcherm.versionedserialization.objects.SimpleV1;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Tests of evolving schemas. */
public class EvolutionTest {

    /** Class to use in a test; adds one optional field. */
    public static class SimpleV1AddOptionalField {
        public String s;
        public int i;
        public Optional<String> extra;
    }

    @Test
    public void testAddOptionalField() {
        final String startingString = """
            {"s":"abc","i":37}""";
        final JsonNode startingNode = SerializationUtil.deserializeAsNode(startingString);

        final String v1Schema = SerializationUtil.generateSchema(SimpleV1.class);
        final String v2Schema = SerializationUtil.generateSchema(SimpleV1AddOptionalField.class);
        final JsonNode migratedNode = Migrate.migrateVersions(v1Schema, v2Schema, startingNode);

        assertEquals("abc", migratedNode.get("s").asText());
        assertEquals(37, migratedNode.get("i").asInt());
        assertTrue(migratedNode.get("extra").isNull());

        final SimpleV1AddOptionalField out = SerializationUtil.deserialize(migratedNode.toString(), SimpleV1AddOptionalField.class);

        assertEquals("abc", out.s);
        assertEquals(37, out.i);
        assertTrue(out.extra.isEmpty());
    }
}
