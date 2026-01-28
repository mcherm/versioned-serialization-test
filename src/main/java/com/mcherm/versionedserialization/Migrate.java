package com.mcherm.versionedserialization;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/** Contains methods for migrating a JSON document from one schema to another. */
public class Migrate {

    /**
     * This is passed two different JSON schemas (currently in the form of Strings), and some JSON data
     * that is guaranteed to conform to jsonSchema1. It is expected to produce some JSON data that
     * conforms to jsonSchema2. It IS allowed to modify the input source.
     */
    public static JsonNode migrateVersions(final String jsonSchema1, final String jsonSchema2, JsonNode sourceRoot) {
        // NOTE: As a temporary step, this is fully hard-coded to work only on specific schemas.
        final ObjectNode sourceRootMut = (ObjectNode) sourceRoot;
        sourceRootMut.set("extra", NullNode.getInstance());
        return sourceRootMut;
    }
}
