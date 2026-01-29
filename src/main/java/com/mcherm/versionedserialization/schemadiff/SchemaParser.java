package com.mcherm.versionedserialization.schemadiff;

import com.fasterxml.jackson.databind.JsonNode;
import com.mcherm.versionedserialization.SerializationUtil;
import com.mcherm.versionedserialization.schemadiff.schema.EnumValues;
import com.mcherm.versionedserialization.schemadiff.schema.PrimitiveType;
import com.mcherm.versionedserialization.schemadiff.schema.Properties;
import com.mcherm.versionedserialization.schemadiff.schema.Reference;
import com.mcherm.versionedserialization.schemadiff.schema.SchemaInfo;
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
 * Attempts to parse a JSON Schema.
 */
public class SchemaParser {

    /**
     * A way of accessing the defs as we build things. It's ThreadLocal and used only
     * during the parse() method and the things that calls. This will support back
     * references (but not forward references). // FIXME: deal with forward references
     */
    private final ThreadLocal<Map<String,Subschema>> globalDefs = new ThreadLocal<>();

    /** Parse a schema. */
    public SchemaInfo parse(String schema) throws UnsupportedSchemaFeature {
        // --- parse to JSON tree ---
        final JsonNode jsonNode = SerializationUtil.deserializeAsNode(schema);

        // --- walk the JSON tree creating stuff ---
        try {
            final String schemaVersion = jsonNode.get("$schema").asText();
            final Map<String,Subschema> defs = parseDefs(jsonNode.get("$defs"));
            // make sure we did the defs before the properties
            final Properties properties = parseProperties(jsonNode.get("properties"));

            return new SchemaInfo(schemaVersion, defs, properties);
        } finally {
            globalDefs.set(null); // clear the threadlocal
        }
    }

    /** Parses a whole list of defs. As a side effect, this sets the threadlocal globalDefs. */
    private Map<String,Subschema> parseDefs(@Nullable JsonNode jsonNode) throws UnsupportedSchemaFeature {
        if (jsonNode == null) {
            // there WAS no jsonNode, which just means there are no definitions.
            return Map.of();
        }
        if (!jsonNode.isObject()) {
            throw new UnsupportedSchemaFeature("$defs must be an object");
        }
        final Map<String,Subschema> defs = new LinkedHashMap<>();
        globalDefs.set(defs); // set the threadlocal. Now we can use it while recursing
        for (var entry : jsonNode.properties()) {
            defs.put(entry.getKey(), parseSubschema(entry.getValue()));
        }
        return defs;
    }

    /** Parses a properties declaration. Is passed the map. */
    private Properties parseProperties(JsonNode jsonNode) throws UnsupportedSchemaFeature {
        if (!jsonNode.isObject()) {
            throw new UnsupportedSchemaFeature("properties must be an object");
        }
        final Map<String, Subschema> propData = new LinkedHashMap<>();
        for (var entry : jsonNode.properties()) {
            propData.put(entry.getKey(), parseSubschema(entry.getValue()));
        }
        return new Properties(propData);
    }

    private Subschema parseSubschema(JsonNode jsonNode) throws UnsupportedSchemaFeature {
        if (!jsonNode.isObject()) {
            throw new UnsupportedSchemaFeature("subschema must be an object");
        }
        Types types = null;
        Properties properties = null;
        Subschema itemsType = null;
        EnumValues enumValues = null;
        Reference reference = null;
        for (var entry : jsonNode.properties() ) {
            switch (entry.getKey()) {
                case "type" -> types = parseTypes(entry.getValue());
                case "properties" -> properties = parseProperties(entry.getValue());
                case "items" -> itemsType = parseSubschema(entry.getValue());
                case "enum" -> enumValues = parseEnumValues(entry.getValue());
                case "$ref" -> reference = parseReference(entry.getValue());
                default -> throw new UnsupportedSchemaFeature("unsupported subschema feature: " + entry.getKey());
            }
        }
        if (reference == null) {
            return new Subschema(types, properties, itemsType, enumValues);
        }

        // --- Handle Reference (which is special) ---
        if (types != null || properties != null | itemsType != null || enumValues != null) {
            throw new UnsupportedSchemaFeature("reference with other fields not supported");
        }
        // FIXME: This handles back-references but not forward references
        final Subschema knownSubschema = globalDefs.get().get(reference.getName());
        if (knownSubschema == null) {
            throw new UnsupportedSchemaFeature("reference to #/$defs/" + reference.getName() + " isn't a backreference");
        }
        return knownSubschema;
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
