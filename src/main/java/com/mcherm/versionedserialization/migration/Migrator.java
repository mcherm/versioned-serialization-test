package com.mcherm.versionedserialization.migration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mcherm.versionedserialization.schemadiff.SchemaDiffer;
import com.mcherm.versionedserialization.schemadiff.deltas.Delta;
import com.mcherm.versionedserialization.schemadiff.deltas.Change;
import com.mcherm.versionedserialization.schemadiff.deltas.CustomAdd;
import com.mcherm.versionedserialization.schemadiff.deltas.DefaultingAdd;
import com.mcherm.versionedserialization.schemadiff.deltas.Drop;
import com.mcherm.versionedserialization.schemadiff.deltas.SchemaDeltas;
import com.mcherm.versionedserialization.schemadiff.path.Lookup;
import com.mcherm.versionedserialization.schemadiff.schema.SchemaInfo;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Class containing a function to migrate a serialized object written for one schema to
 * the correct form for a new schema, following a set of UpdateRules.
 */
public class Migrator {

    /**
     * This takes a source and target schema, a JSON document in the source format, and a
     * set of update rules that define ambiguous cases and/or overrides of migration rules.
     * It applies the rules (as well as any changes that can be automatically derived from
     * the schemas) to produce an updated document in the target format.
     *
     * @param sourceSchema schema for the source document
     * @param targetSchema schema we want the target document to take on
     * @param sourceDocument source JSON document
     * @param updateRules collection of UpdateRules to use
     * @return the transformed target document
     */
    public JsonNode migrate(
            final SchemaInfo sourceSchema,
            final SchemaInfo targetSchema,
            final JsonNode sourceDocument,
            final UpdateRules updateRules
    ) {
        final JsonNode destinationDocument = sourceDocument.deepCopy();

        final UpdateContext updateContext = new UpdateContext(sourceSchema, targetSchema, sourceDocument);
        final SchemaDeltas schemaDeltas = SchemaDiffer.diff(sourceSchema, targetSchema);
        for (Delta delta : schemaDeltas.getDeltas()) {
            final String fieldName = delta.getFieldName();
            final Optional<UpdateRule> updateRule = updateRules.getUpdateRule(fieldName);
            applyChange(updateContext, delta, updateRule, destinationDocument);
        }
        return destinationDocument;
    }

    /**
     * This is passed a Delta and either an UpdateRule or Empty (and if the UpdateRule
     * requires customization the updateRule will never be Empty). It is also passed a
     * mutable destinationDocument.
     * <p>
     * The delta's fieldName may contain "[]" segments indicating arrays that need
     * to be iterated over. This method splits on "[]" and delegates to
     * {@link #applyAcrossArrays} to handle the recursion.
     *
     * @param updateContext an UpdateContext which can be used to call UpdateRule.mapField()
     * @param delta the particular Delta to be processed
     * @param updateRule either an UpdateRule that applies or Empty; and it will never be
     *                   empty if the Delta requires customization.
     * @param destinationDocument the new JSON document that is being constructed and which
     *                            this method will modify.
     */
    private void applyChange(
            final UpdateContext updateContext,
            final Delta delta,
            final Optional<UpdateRule> updateRule,
            final JsonNode destinationDocument
    ) {
        assert !delta.requiresCustomization() || updateRule.isPresent();
        final List<String> segments = Arrays.asList(delta.getFieldName().split("\\[]"));
        applyAcrossArrays(updateContext, delta, updateRule, destinationDocument, segments);
    }

    /**
     * Recursively processes segments of a fieldName that has been split on "[]".
     * If only one segment remains, this is the leaf case and we apply the actual change
     * via {@link #applyLeafChange}. Otherwise, the first segment is a path to an array
     * node; we navigate to it, then loop over each element and recurse with the
     * remaining segments.
     *
     * @param updateContext an UpdateContext which can be used to call UpdateRule.mapField()
     * @param delta the particular Delta to be processed
     * @param updateRule either an UpdateRule that applies or Empty
     * @param currentNode the node to navigate from
     * @param remainingSegments the segments still to be processed
     */
    private void applyAcrossArrays(
            final UpdateContext updateContext,
            final Delta delta,
            final Optional<UpdateRule> updateRule,
            final JsonNode currentNode,
            final List<String> remainingSegments
    ) {
        if (remainingSegments.size() == 1) {
            applyLeafChange(updateContext, delta, updateRule, currentNode, remainingSegments.getFirst());
        } else {
            final String pathToArray = remainingSegments.getFirst();
            final JsonNode arrayNode = Lookup.getField(pathToArray, currentNode)
                    .orElseThrow(() -> new RuntimeException(
                            "Cannot navigate to array at '" + pathToArray + "' in the document."
                    ));
            if (!arrayNode.isArray()) {
                throw new RuntimeException(
                        "Expected an array at '" + pathToArray + "' but found " + arrayNode.getNodeType()
                );
            }
            final List<String> rest = remainingSegments.subList(1, remainingSegments.size());
            for (JsonNode element : arrayNode) {
                applyAcrossArrays(updateContext, delta, updateRule, element, rest);
            }
        }
    }

    /**
     * Applies a single delta at a leaf location in the JSON tree. The relativePath
     * is a "/"-separated path (no "[]") relative to baseNode.
     *
     * @param updateContext an UpdateContext which can be used to call UpdateRule.mapField()
     * @param delta the particular Delta to be processed
     * @param updateRule either an UpdateRule that applies or Empty; and it will never be
     *                   empty if the Delta requires customization.
     * @param baseNode the node to navigate from using relativePath
     * @param relativePath a "/"-separated path to the field to modify
     */
    private void applyLeafChange(
            final UpdateContext updateContext,
            final Delta delta,
            final Optional<UpdateRule> updateRule,
            final JsonNode baseNode,
            final String relativePath
    ) {
        // --- Navigate to the proper location in the Json ---
        final Lookup.ParentAndField parentAndField = Lookup.getParentAndField(
                relativePath, baseNode
        ).orElseThrow(() -> new RuntimeException(
                "Cannot navigate to field '" + relativePath + "' in the document."
        ));
        final ObjectNode parentNode = parentAndField.parentNode();
        final Optional<JsonNode> existingNode = parentAndField.targetNode();
        final String fieldInParent = parentAndField.fieldInParent();

        assert existingNode.equals(Optional.ofNullable(parentNode.get(fieldInParent)));

        // --- Apply the change ---
        switch (delta) {
            case Drop drop -> {
                // Remove the existing node (always)
                parentNode.remove(fieldInParent);
            }
            case DefaultingAdd defaultingAdd -> {
                final JsonNode newNode;
                if (updateRule.isPresent()) {
                    newNode = updateRule.get().mapField(updateContext, delta.getFieldName());
                } else {
                    newNode = defaultingAdd.getDefaultValue();
                }
                parentNode.set(fieldInParent, newNode);
            }
            case CustomAdd customAdd -> {
                assert updateRule.isPresent();
                final JsonNode newNode = updateRule.get().mapField(updateContext, delta.getFieldName());
                parentNode.set(fieldInParent, newNode);
            }
            case Change change -> {
                assert updateRule.isPresent();
                final JsonNode newNode = updateRule.get().mapField(updateContext, delta.getFieldName());
                parentNode.set(fieldInParent, newNode);
            }
        }
    }
}
