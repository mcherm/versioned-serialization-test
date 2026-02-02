package com.mcherm.versionedserialization.migration;

import com.fasterxml.jackson.databind.JsonNode;
import com.mcherm.versionedserialization.schemadiff.path.Lookup;
import com.mcherm.versionedserialization.schemadiff.schema.SchemaInfo;

import java.util.Optional;

/**
 * This contains the information that's available to an UpdateRule.
 */
public class UpdateContext {
    private final JsonNode sourceDocument;
    private final SchemaInfo sourceSchema;
    private final SchemaInfo targetSchema;

    public UpdateContext(
            final SchemaInfo sourceSchema,
            final SchemaInfo targetSchema,
            final JsonNode sourceDocument
    ) {
        this.sourceSchema = sourceSchema;
        this.targetSchema = targetSchema;
        this.sourceDocument = sourceDocument;
    }

    public JsonNode getSourceDocument() {
        return sourceDocument;
    }

    public SchemaInfo getTargetSchema() {
        return targetSchema;
    }

    public SchemaInfo getSourceSchema() {
        return sourceSchema;
    }

    public Optional<JsonNode> getSourceValue(String fieldName) {
        return Lookup.getField(fieldName, sourceDocument);
    }
}
