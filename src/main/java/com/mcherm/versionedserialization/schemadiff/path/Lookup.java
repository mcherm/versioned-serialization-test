package com.mcherm.versionedserialization.schemadiff.path;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Optional;

/**
 * This class contains some functions for looking up a field name within a JSON document.
 */
public class Lookup {

    /**
     * The result of navigating to a field's parent in a JSON document. Contains the
     * parent ObjectNode, the field name within that parent, and the current value of
     * that field (if any).
     */
    public record ParentAndField(ObjectNode parentNode, String fieldInParent, Optional<JsonNode> targetNode) {}

    /**
     * Given a field name (using backslash-separated path components) and a JSON document,
     * this navigates to the parent of the indicated field and returns a {@link ParentAndField}
     * containing the parent node, the final path component, and the current value of that
     * field (as an Optional that is Empty if the field does not yet exist in the parent).
     *
     * <p>For now, paths with square brackets in them are not supported and will return Empty.
     *
     * @param fieldName the backslash-separated path to the field
     * @param document the JSON document to navigate
     * @return a ParentAndField if navigation succeeded, or Empty if it could not be resolved
     */
    public static Optional<ParentAndField> getParentAndField(final String fieldName, final JsonNode document) {
        if (fieldName.contains("[]")) {
            return Optional.empty();
        }
        final String[] components = fieldName.split("/");
        JsonNode currentNode = document;
        // Navigate to the parent (all components except the last)
        for (int i = 0; i < components.length - 1; i++) {
            if (currentNode.isObject()) {
                currentNode = currentNode.get(components[i]);
                if (currentNode == null) {
                    return Optional.empty();
                }
            } else {
                return Optional.empty();
            }
        }
        if (!currentNode.isObject()) {
            return Optional.empty();
        }
        final String lastComponent = components[components.length - 1];
        final Optional<JsonNode> targetNode = Optional.ofNullable(currentNode.get(lastComponent));
        return Optional.of(new ParentAndField((ObjectNode) currentNode, lastComponent, targetNode));
    }

    /**
     * Given a string telling a particular field and a JSON document, this returns the value
     * of that field in the document (as a JsonNode) or returns Empty to indicate that it
     * does not exist in the document.
     *
     * <p>For now, paths with square brackets in them simply won't succeed ever.
     *
     * @param fieldName the backslash-separated path to the field
     * @param document the JSON document to navigate
     * @return the JsonNode at that path, or Empty if it does not exist
     */
    public static Optional<JsonNode> getField(final String fieldName, final JsonNode document) {
        return getParentAndField(fieldName, document)
                .flatMap(pf -> pf.targetNode());
    }
}
