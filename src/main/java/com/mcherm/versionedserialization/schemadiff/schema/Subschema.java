package com.mcherm.versionedserialization.schemadiff.schema;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A subschema... what things can be defined to be. We are applying some limitations,
 * like that subschemas don't have their own defs section.
 */
// FIXME: Doesn't handle self-references
// FIXME: Doesn't handle allOf, anyOf, oneOf
public class Subschema {
    @Nullable
    private final Types types;
    @Nullable
    private final Properties properties;
    @Nullable
    private final Subschema itemsType;
    @Nullable
    private final EnumValues enumValues;


    /** Constructor */
    public Subschema(
            @Nullable Types types,
            @Nullable Properties properties,
            @Nullable Subschema itemsType,
            @Nullable EnumValues enumValues
    ) {
        this.types = types;
        this.properties = properties;
        this.itemsType = itemsType;
        this.enumValues = enumValues;
    }

    public @Nullable Types getTypes() {
        return types;
    }

    public @Nullable Properties getProperties() {
        return properties;
    }

    public @Nullable Subschema getItemsType() {
        return itemsType;
    }

    public @Nullable EnumValues getEnumValues() {
        return enumValues;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Subschema subschema)) return false;
        return Objects.equals(types, subschema.types) && Objects.equals(properties, subschema.properties) && Objects.equals(itemsType, subschema.itemsType) && Objects.equals(enumValues, subschema.enumValues);
    }

    @Override
    public int hashCode() {
        return Objects.hash(types, properties, itemsType, enumValues);
    }

    @Override
    public String toString() {
        final List<String> fields = new ArrayList<>();
        if (types != null) {
            fields.add("\"type\":" + types);
        }
        if (properties != null) {
            fields.add("\"properties\":" + properties);
        }
        if (itemsType != null) {
            fields.add("\"items\":" + itemsType);
        }
        if (enumValues != null) {
            fields.add("\"enum\":" + enumValues);
        }
        return "{" + String.join(",", fields) + "}";
    }
}
