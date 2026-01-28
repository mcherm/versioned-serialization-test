package com.mcherm.versionedserialization;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Tests of JsonUtil that aren't covered elsewhere. */
public class SerializationUtilTest {

    @Test
    public void testDeserializeAsNode() {
        final String source = "{\"s\":\"abc\",\"i\":37}";
        final JsonNode rootNode = SerializationUtil.deserializeAsNode(source);
        assertEquals("abc", rootNode.get("s").asText());
        assertEquals(37, rootNode.get("i").asInt());
    }
}
