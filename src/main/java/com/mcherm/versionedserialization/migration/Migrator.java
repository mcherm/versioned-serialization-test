package com.mcherm.versionedserialization.migration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mcherm.versionedserialization.schemadiff.SchemaDiffer;
import com.mcherm.versionedserialization.schemadiff.deltas.Alteration;
import com.mcherm.versionedserialization.schemadiff.deltas.Change;
import com.mcherm.versionedserialization.schemadiff.deltas.CustomAdd;
import com.mcherm.versionedserialization.schemadiff.deltas.DefaultingAdd;
import com.mcherm.versionedserialization.schemadiff.deltas.Drop;
import com.mcherm.versionedserialization.schemadiff.deltas.SchemaDeltas;
import com.mcherm.versionedserialization.schemadiff.path.Lookup;
import com.mcherm.versionedserialization.schemadiff.schema.SchemaInfo;

import java.util.Optional;

public class Migrator {

    public JsonNode migrate(
            final SchemaInfo sourceSchema,
            final SchemaInfo targetSchema,
            final JsonNode sourceDocument,
            final UpdateRules updateRules
    ) {
        final JsonNode destinationDocument = sourceDocument.deepCopy();

        final UpdateContext updateContext = new UpdateContext(sourceSchema, targetSchema, sourceDocument);
        final SchemaDeltas schemaDeltas = SchemaDiffer.diff(sourceSchema, targetSchema);
        for (Alteration alteration : schemaDeltas.getDeltas()) {
            final String fieldName = alteration.getFieldName();
            final Optional<UpdateRule> updateRule = updateRules.getUpdateRule(fieldName);
            applyChange(updateContext, alteration, updateRule, destinationDocument);
        }
        return destinationDocument;
    }

    /**
     * This is passed an Alteration and either an UpdateRule or Empty (and if the UpdateRule
     * requires customization the updateRule will never be Empty). It is also passed a
     * mutable destinationDocument.
     *
     * @param updateContext an UpdateContext which can be used to call UpdateRule.mapField()
     * @param alteration the particular Alteration to be processed
     * @param updateRule either an UpdateRule that applies or Empty; and it will never be
     *                   empty if the Alteration requires customization.
     * @param destinationDocument the new JSON document that is being constructed and which
     *                            this method will modify.
     */
    private void applyChange(
            final UpdateContext updateContext,
            final Alteration alteration,
            final Optional<UpdateRule> updateRule,
            final JsonNode destinationDocument
    ) {
        assert !alteration.requiresCustomization() || updateRule.isPresent();

        // --- Navigate to the proper location in the Json ---
        final Lookup.ParentAndField parentAndField = Lookup.getParentAndField(
                alteration.getFieldName(), destinationDocument
        ).orElseThrow(() -> new RuntimeException(
                "Cannot navigate to field '" + alteration.getFieldName() + "' in the document."
        ));
        final ObjectNode parentNode = parentAndField.parentNode();
        final Optional<JsonNode> existingNode = parentAndField.targetNode();
        final String fieldInParent = parentAndField.fieldInParent();

        assert existingNode.equals(Optional.ofNullable(parentNode.get(fieldInParent)));

        // --- Apply the change ---
        switch (alteration) {
            case Drop drop -> {
                // Remove the existing node (always)
                parentNode.remove(fieldInParent);
            }
            case DefaultingAdd defaultingAdd -> {
                final JsonNode newNode;
                if (updateRule.isPresent()) {
                    newNode = updateRule.get().mapField(updateContext, alteration.getFieldName());
                } else {
                    newNode = defaultingAdd.getDefaultValue();
                }
                parentNode.set(fieldInParent, newNode);
            }
            case CustomAdd customAdd -> {
                assert updateRule.isPresent();
                final JsonNode newNode = updateRule.get().mapField(updateContext, alteration.getFieldName());
                parentNode.set(fieldInParent, newNode);
            }
            case Change change -> {
                assert updateRule.isPresent();
                final JsonNode newNode = updateRule.get().mapField(updateContext, alteration.getFieldName());
                parentNode.set(fieldInParent, newNode);
            }
        }
    }
}
