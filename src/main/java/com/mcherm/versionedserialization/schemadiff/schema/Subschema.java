package com.mcherm.versionedserialization.schemadiff.schema;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A subschema... what things can be defined to be. We are applying some limitations,
 * like that subschemas don't have their own defs section.
 */
// FIXME: Maybe should break into subclasses for Reference, SelfReference, and Regular?
// FIXME: Doesn't handle allOf, anyOf, oneOf
public class Subschema {
    private final boolean isResolved;
    private final boolean inSelfReference;
    @Nullable
    private final Reference reference; // if non-null, everything else is null
    @Nullable
    private final String selfReferenceName; // if non-null everything else is null
    @Nullable
    private final Types types;
    @Nullable
    private final Properties properties;
    @Nullable
    private final Subschema itemsType;
    @Nullable
    private final EnumValues enumValues;


    /** Constructor */
    private Subschema(
            final boolean isResolved,
            final boolean inSelfReference,
            @Nullable final Reference reference,
            @Nullable final String selfReferenceName,
            @Nullable final Types types,
            @Nullable final Properties properties,
            @Nullable final Subschema itemsType,
            @Nullable final EnumValues enumValues
    ) {
        this.isResolved = isResolved;
        this.inSelfReference = inSelfReference;
        this.selfReferenceName = selfReferenceName;
        this.reference = reference;
        this.types = types;
        this.properties = properties;
        this.itemsType = itemsType;
        this.enumValues = enumValues;
    }

    public static Subschema fromFields(
            final boolean inSelfReference,
            @Nullable Types types,
            @Nullable Properties properties,
            @Nullable Subschema itemsType,
            @Nullable EnumValues enumValues
    ) {
        final boolean isResolved =
                (itemsType == null || itemsType.isResolved())
                        && (properties == null || properties.allResolved());
        final Reference reference = null;
        return new Subschema(isResolved, inSelfReference, reference, null, types, properties, itemsType, enumValues);
    }

    /** Factory Function */
    public static Subschema fromReference(
            final Reference reference
    ) {
        final boolean isResolved = false;
        final boolean inSelfReference = false;
        return new Subschema(isResolved, inSelfReference, reference, null, null, null, null, null);
    }

    /** Factory Function */
    public static Subschema fromSelfReference(
            final String selfReferenceName
    ) {
        final boolean isResolved = true;
        final boolean inSelfReference = true;
        return new Subschema(isResolved, inSelfReference, null, selfReferenceName, null, null, null, null);
    }

    public boolean isResolved() {
        return isResolved;
    }

    public boolean isInSelfReference() {
        return inSelfReference;
    }

    public @Nullable String getSelfReferenceName() {
        return selfReferenceName;
    }

    public @Nullable Reference getReference() {
        return reference;
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
        if (reference != null) {
            return "{\"$ref\":\"#/$defs/" + reference.getName() + "\"}";
        }
        if (selfReferenceName != null) {
            return "{\"$selfRef\":\"" + selfReferenceName + "\"}";
        }
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
        return "{" +
                (inSelfReference ? "*" : "") + // FIXME: Remove eventually, just marks the inSelfReference items
                String.join(",", fields) + "}";
    }
}
