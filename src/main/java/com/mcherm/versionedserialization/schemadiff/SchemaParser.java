package com.mcherm.versionedserialization.schemadiff;

import com.fasterxml.jackson.databind.JsonNode;
import com.mcherm.versionedserialization.SerializationUtil;
import com.mcherm.versionedserialization.schemadiff.schema.AnyOfSubschema;
import com.mcherm.versionedserialization.schemadiff.schema.EnumValues;
import com.mcherm.versionedserialization.schemadiff.schema.NormalSubschema;
import com.mcherm.versionedserialization.schemadiff.schema.PrimitiveType;
import com.mcherm.versionedserialization.schemadiff.schema.Properties;
import com.mcherm.versionedserialization.schemadiff.schema.Reference;
import com.mcherm.versionedserialization.schemadiff.schema.SchemaInfo;
import com.mcherm.versionedserialization.schemadiff.schema.SelfReference;
import com.mcherm.versionedserialization.schemadiff.schema.Subschema;
import com.mcherm.versionedserialization.schemadiff.schema.Types;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This object is used to parse a JSON Schema file.
 */
public class SchemaParser {

    /**
     * Parse a schema. This is passed the contents of a JSON Schema file (as a String) and
     * it returns a SchemaInfo object, which is a parsed form of it. If it runs into anything
     * it can't handle it throws an UnsupportedSchemaFeature exception instead.
     */
    public static SchemaInfo parse(String schema) throws UnsupportedSchemaFeature {
        final SchemaParser schemaParser = new SchemaParser();
        return schemaParser.parseSchema(schema);
    }

    /**
     * Parse a schema. This is passed the contents of a JSON Schema file (as a String) and
     * it returns a SchemaInfo object, which is a parsed form of it. If it runs into anything
     * it can't handle it throws an UnsupportedSchemaFeature exception instead.
     */
    public SchemaInfo parseSchema(String schema) throws UnsupportedSchemaFeature {
        // --- parse to JSON tree ---
        final JsonNode jsonNode = SerializationUtil.deserializeAsNode(schema);

        // --- walk the JSON tree creating stuff ---
        final String schemaVersion = jsonNode.get("$schema").asText();
        final Map<String,Subschema> defs = parseDefs(jsonNode.get("$defs"));
        fixForwardReferences(defs);
        // make sure we did the defs before the properties
        final Properties properties = parseProperties(defs, jsonNode.get("properties"));

        return new SchemaInfo(schemaVersion, defs, properties);
    }

    /** Parses a whole list of defs. The list it returns may contain unresolved References. */
    private Map<String,Subschema> parseDefs(@Nullable JsonNode jsonNode) throws UnsupportedSchemaFeature {
        if (jsonNode == null) {
            // there WAS no jsonNode, which just means there are no definitions.
            return Map.of();
        }
        if (!jsonNode.isObject()) {
            throw new UnsupportedSchemaFeature("$defs must be an object");
        }
        final Map<String,Subschema> defs = new LinkedHashMap<>();
        for (var entry : jsonNode.properties()) {
            defs.put(entry.getKey(), parseSubschema(defs, entry.getValue()));
        }
        return defs;
    }

    /**
     * A class containing the data we want to keep around while we do the resolution pass
     * where any forward references are resolved to their proper values (or perhaps to a
     * self-reference).
     */
    private static class ResolutionState {
        private final List<String> nameStack;
        private final Set<String> loopsToClear;
        /** Constructor passing one name for the stack. */
        public ResolutionState(final String name) {
            nameStack = new ArrayList<>();
            nameStack.add(name);
            loopsToClear = new LinkedHashSet<>();
        }
        public void pushName(String name) {
            nameStack.add(name);
        }
        public void popName() {
            final String poppedName = nameStack.removeLast();
            nameStack.remove(poppedName);
        }
        public void addLoopToClear(String name) {
            loopsToClear.add(name);
        }
        public void markLoopCleared(String name) {
            loopsToClear.remove(name);
        }
        public boolean hasSelfReferences() {
            return !loopsToClear.isEmpty();
        }
        public List<String> getNameStack() {
            return nameStack;
        }

        @Override
        public String toString() {
            return "ResolutionState{" +
                    "nameStack=" + nameStack +
                    ", loopsToClear=" + loopsToClear +
                    '}';
        }
    }


    /**
     * This modifies the defs by finding any References within it and replacing them with
     * the actual contents as a Subschema. The Subschema might contain a self-reference,
     * but none of them will contain a Reference.
     *
     * @param defs the defs which may contain References when we start
     */
    private void fixForwardReferences(
            final Map<String,Subschema> defs
    ) throws UnsupportedSchemaFeature {
        final Set<String> keysToBeFixed = new LinkedHashSet<>(defs.keySet());
        while (!keysToBeFixed.isEmpty()) {
            final String keytowork = keysToBeFixed.iterator().next();
            keysToBeFixed.remove(keytowork);
            final Subschema oldSubschema = defs.get(keytowork);
            final ResolutionState resolutionState = new ResolutionState(keytowork);
            final Subschema resolvedSubschema = resolveReferencesInSubschema(defs, resolutionState, oldSubschema);
            defs.put(keytowork, resolvedSubschema);
        }
    }

    /**
     * This resolves forward references within a single subschema. If, along the way, it resolves
     * an entry in defs it will modify that as it goes.
     *
     * @param defs the defs (used to resolve references)
     * @param resolutionState pass around state while doing the resolution of forward references
     * @param existingSubschema the current Subschema that we are patching up (or leaving as-is!)
     * @return the updated Subschema
     */
    private Subschema resolveReferencesInSubschema(
            final Map<String,Subschema> defs,
            final ResolutionState resolutionState,
            final Subschema existingSubschema
    ) throws UnsupportedSchemaFeature {
        if (existingSubschema.isResolved()) {
            return existingSubschema;
        }
        switch(existingSubschema) {
            case SelfReference selfReference -> throw new RuntimeException(
                    "It should never be resolving something that already contains a SelfReference");
            case NormalSubschema existingNormalSubschema -> {
                // --- handle a non-reference ---
                final Types types = existingNormalSubschema.getTypes();
                Properties properties = existingNormalSubschema.getProperties();
                if (properties != null) {
                    properties = resolveReferencesInProperties(defs, resolutionState, properties);
                }
                Subschema itemsType = existingNormalSubschema.getItemsType();
                if (itemsType != null) {
                    itemsType = resolveReferencesInSubschema(defs, resolutionState, itemsType);
                }
                final EnumValues enumValues = existingNormalSubschema.getEnumValues();
                final String javaType = existingNormalSubschema.getJavaType();
                final String constValue = existingNormalSubschema.getConstValue();

                final boolean isInSelfReference = resolutionState.hasSelfReferences();
                return NormalSubschema.fromFields(isInSelfReference, types, properties, itemsType, enumValues, javaType, constValue);
            }
            case AnyOfSubschema anyOfSubschema -> {
                // --- handle anyOf: resolve each option ---
                final List<Subschema> resolvedOptions = new ArrayList<>();
                for (Subschema option : anyOfSubschema.getOptions()) {
                    resolvedOptions.add(resolveReferencesInSubschema(defs, resolutionState, option));
                }
                return new AnyOfSubschema(resolvedOptions, anyOfSubschema.getJavaType());
            }
            case Reference reference -> {
                // --- handle a reference ---
                // ----- check for self-reference ----
                final String referenceName = reference.getName();
                if (resolutionState.getNameStack().contains(referenceName)) {
                    resolutionState.addLoopToClear(referenceName);
                    final Subschema resolvedSubschema = SelfReference.fromSelfReference(referenceName);
                    return resolvedSubschema;
                }
                // ----- retrieve from defs -----
                final Subschema definedSubschema = defs.get(referenceName);
                if (definedSubschema == null) {
                    throw new UnsupportedSchemaFeature("Reference #/$defs/" + referenceName + " not found.");
                }
                if (definedSubschema.isResolved()) {
                    return applyOverridesFromReference(reference, definedSubschema);
                }
                // ----- resolve it -----
                resolutionState.pushName(referenceName);
                final Subschema resolvedSubschema = resolveReferencesInSubschema(defs, resolutionState, definedSubschema);
                resolutionState.popName();
                // ----- record in defs (giving us memoization) -----
                defs.put(referenceName, resolvedSubschema);
                resolutionState.markLoopCleared(referenceName);
                // ----- all done -----
                return applyOverridesFromReference(reference, resolvedSubschema);
            }
        }
    }

    /**
     * This resolves forward references within a single Properties. If, along the way, it resolves
     * an entry in defs it will modify that as it goes.
     *
     * @param defs the defs (used to resolve references)
     * @param resolutionState pass around state while doing the resolution of forward references
     * @param existingProperties the current Properties that we are patching up (or leaving as-is!)
     * @return the updated Properties
     */
    private Properties resolveReferencesInProperties(
            final Map<String,Subschema> defs,
            final ResolutionState resolutionState,
            final Properties existingProperties
    ) throws UnsupportedSchemaFeature {
        if (existingProperties.allResolved()) {
            return existingProperties;
        }
        final Map<String, Subschema> resolvedProperties = new LinkedHashMap<>();
        for (Map.Entry<String, Subschema> entry : existingProperties.getProperties().entrySet()) {
            final Subschema resolvedSubschema = resolveReferencesInSubschema(defs, resolutionState, entry.getValue());
            resolvedProperties.put(entry.getKey(), resolvedSubschema);
        }
        return new Properties(resolvedProperties);
    }

    /**
     * Applies any overrides carried by a Reference (javaType, sibling types/properties)
     * to the resolved Subschema.
     */
    private Subschema applyOverridesFromReference(final Reference reference, final Subschema resolved) {
        if (resolved instanceof NormalSubschema normalSubschema) {
            if (reference.hasOverrides()) {
                return normalSubschema.withMergedOverrides(
                        reference.getOverrideTypes(), reference.getOverrideProperties(), reference.getJavaType());
            }
            if (reference.getJavaType() != null) {
                return normalSubschema.withJavaType(reference.getJavaType());
            }
        }
        return resolved;
    }

    /**
     * Parses a properties declaration.
     *
     * @param defs the defs in their current state. This may get mutated by the method
     * @param jsonNode the node containing the properties map
     * @return the new Properties object
     */
    private Properties parseProperties(
            final Map<String,Subschema> defs,
            final JsonNode jsonNode
    ) throws UnsupportedSchemaFeature {
        if (!jsonNode.isObject()) {
            throw new UnsupportedSchemaFeature("properties must exist at top of schema");
        }
        if (!jsonNode.isObject()) {
            throw new UnsupportedSchemaFeature("properties must be an object");
        }
        final Map<String, Subschema> propData = new LinkedHashMap<>();
        for (var entry : jsonNode.properties()) {
            final Subschema subschema = parseSubschema(defs, entry.getValue());
            propData.put(entry.getKey(), subschema);
        }
        return new Properties(propData);
    }

    /**
     * Parse a node containing a subschema.
     *
     * @param defs the defs in their current state. This may get mutated by the method
     * @param jsonNode the node containing the subschema
     * @return the newly parsed Subschema
     */
    private Subschema parseSubschema(
            final Map<String,Subschema> defs,
            final JsonNode jsonNode
    ) throws UnsupportedSchemaFeature {
        if (!jsonNode.isObject()) {
            throw new UnsupportedSchemaFeature("subschema must be an object");
        }
        Types types = null;
        Properties properties = null;
        Subschema itemsType = null;
        EnumValues enumValues = null;
        Reference reference = null;
        String javaType = null;
        String constValue = null;
        List<Subschema> anyOfOptions = null;
        for (var entry : jsonNode.properties() ) {
            switch (entry.getKey()) {
                case "type" -> types = parseTypes(entry.getValue());
                case "properties" -> properties = parseProperties(defs, entry.getValue());
                case "items" -> itemsType = parseSubschema(defs, entry.getValue());
                case "enum" -> enumValues = parseEnumValues(entry.getValue());
                case "const" -> constValue = entry.getValue().asText();
                case "required" -> {} // ignore the "required" property
                case "format" -> {} // ignore the "format" property
                case "anyOf" -> anyOfOptions = parseAnyOf(defs, entry.getValue());
                case "$ref" -> reference = parseReference(entry.getValue());
                case "x-javaType" -> javaType = entry.getValue().asText();
                default -> {
                    // Silently ignore other JSON Schema extension properties
                    if (!entry.getKey().startsWith("x-")) {
                        throw new UnsupportedSchemaFeature("unsupported subschema feature: " + entry.getKey());
                    }
                }
            }
        }

        // --- Handle anyOf (standalone, not combined with other structural fields) ---
        if (anyOfOptions != null) {
            if (types != null || properties != null || itemsType != null || enumValues != null || reference != null) {
                throw new UnsupportedSchemaFeature("anyOf combined with other structural fields not yet supported");
            }
            return new AnyOfSubschema(anyOfOptions, javaType);
        }

        if (reference == null) {
            final boolean isInSelfReference = false;
            return NormalSubschema.fromFields(isInSelfReference, types, properties, itemsType, enumValues, javaType, constValue);
        }

        // --- Handle Reference (which is special) ---
        final boolean hasSiblingFields = types != null || properties != null || itemsType != null || enumValues != null;
        if (hasSiblingFields) {
            // $ref with sibling fields (JSON Schema 2020-12): merge when resolving.
            final Subschema knownSubschema = defs.get(reference.getName());
            if (knownSubschema != null && knownSubschema instanceof NormalSubschema knownNormal) {
                // Back-reference: merge immediately
                return knownNormal.withMergedOverrides(types, properties, javaType);
            } else {
                // Forward reference: store overrides on Reference for later merge
                return new Reference(reference.getName(), javaType, types, properties);
            }
        }
        final Subschema knownSubschema = defs.get(reference.getName());
        if (knownSubschema != null) {
            // It's a back-reference, and we can go ahead and use it now.
            if (javaType != null && knownSubschema instanceof NormalSubschema normalSubschema) {
                return normalSubschema.withJavaType(javaType);
            }
            return knownSubschema;
        } else {
            // It's a forward reference, so we need to use the reference and resolve it in a later pass
            return new Reference(reference.getName(), javaType);
        }
    }

    private List<Subschema> parseAnyOf(
            final Map<String,Subschema> defs,
            final JsonNode jsonNode
    ) throws UnsupportedSchemaFeature {
        if (!jsonNode.isArray()) {
            throw new UnsupportedSchemaFeature("anyOf must be an array");
        }
        final List<Subschema> options = new ArrayList<>();
        var iter = jsonNode.elements();
        while (iter.hasNext()) {
            options.add(parseSubschema(defs, iter.next()));
        }
        return options;
    }

    private Types parseTypes(JsonNode jsonNode) throws UnsupportedSchemaFeature {
        if (jsonNode.isTextual()) {
            return new Types(Set.of(parsePrimitiveType(jsonNode)));
        }
        if (jsonNode.isArray()) {
            final LinkedHashSet<PrimitiveType> primitives = new LinkedHashSet<>();
            var iter = jsonNode.elements();
            while (iter.hasNext()) {
                primitives.add(parsePrimitiveType(iter.next()));
            }
            return new Types(primitives);
        }
        throw new UnsupportedSchemaFeature("type must be string or list of strings");
    }

    private PrimitiveType parsePrimitiveType(JsonNode jsonNode) throws UnsupportedSchemaFeature {
        if (!jsonNode.isTextual()) {
            throw new UnsupportedSchemaFeature("single type must be string");
        }
        return switch (jsonNode.asText()) {
            case "object" -> PrimitiveType.OBJECT;
            case "array" -> PrimitiveType.ARRAY;
            case "string" -> PrimitiveType.STRING;
            case "number" -> PrimitiveType.NUMBER;
            case "integer" -> PrimitiveType.INTEGER;
            case "boolean" -> PrimitiveType.BOOLEAN;
            case "null" -> PrimitiveType.NULL;
            default -> throw new UnsupportedSchemaFeature("unsupported primitive type: " + jsonNode.asText());
        };
    }

    private EnumValues parseEnumValues(JsonNode jsonNode) throws UnsupportedSchemaFeature {
        if (!jsonNode.isArray()) {
            throw new UnsupportedSchemaFeature("enum value must be an array");
        }
        final List<String> values = new ArrayList<>();
        var iter = jsonNode.elements();
        while (iter.hasNext()) {
            final JsonNode enumValueNode = iter.next();
            if (!enumValueNode.isTextual()) {
                throw new UnsupportedSchemaFeature("enum values must be strings");
            }
            values.add(enumValueNode.asText());
        }
        return new EnumValues(values);
    }

    private Reference parseReference(JsonNode jsonNode) throws UnsupportedSchemaFeature {
        if (!jsonNode.isTextual()) {
            throw new UnsupportedSchemaFeature("reference must be defined with a string");
        }
        final String REQUIRED_PREFIX = "#/$defs/";
        if (!jsonNode.asText().startsWith(REQUIRED_PREFIX)) {
            throw new UnsupportedSchemaFeature("reference must start with \"" + REQUIRED_PREFIX + "\"");
        }
        final String name = jsonNode.asText().substring(REQUIRED_PREFIX.length());
        if (name.isEmpty()) {
            throw new UnsupportedSchemaFeature("reference must have a name");
        }
        return new Reference(name);
    }
}
