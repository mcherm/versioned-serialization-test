package com.mcherm.versionedserialization.schemadiff.schema;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A NormalSubschema is one that isn't a reference or self-reference, just the description of a
 * specific schema.
 */
public final class NormalSubschema implements Subschema {
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
    @Nullable
    private final String javaType;
    @Nullable
    private final String constValue;


    /** Constructor */
    private NormalSubschema(
            final boolean isResolved,
            final boolean inSelfReference,
            @Nullable final Reference reference,
            @Nullable final String selfReferenceName,
            @Nullable final Types types,
            @Nullable final Properties properties,
            @Nullable final Subschema itemsType,
            @Nullable final EnumValues enumValues,
            @Nullable final String javaType,
            @Nullable final String constValue
    ) {
        this.isResolved = isResolved;
        this.inSelfReference = inSelfReference;
        this.selfReferenceName = selfReferenceName;
        this.reference = reference;
        this.types = types;
        this.properties = properties;
        this.itemsType = itemsType;
        this.enumValues = enumValues;
        this.javaType = javaType;
        this.constValue = constValue;
    }

    public static NormalSubschema fromFields(
            final boolean inSelfReference,
            @Nullable final Types types,
            @Nullable final Properties properties,
            @Nullable final Subschema itemsType,
            @Nullable final EnumValues enumValues,
            @Nullable final String javaType,
            @Nullable final String constValue
    ) {
        final boolean isResolved =
                (itemsType == null || itemsType.isResolved())
                        && (properties == null || properties.allResolved());
        final Reference reference = null;
        return new NormalSubschema(isResolved, inSelfReference, reference, null, types, properties, itemsType, enumValues, javaType, constValue);
    }

    /**
     * Returns a copy of this NormalSubschema with the given javaType set.
     * Used when resolving $ref nodes that carry an x-javaType annotation.
     */
    public NormalSubschema withJavaType(@Nullable final String javaType) {
        return new NormalSubschema(isResolved, inSelfReference, reference, selfReferenceName, types, properties, itemsType, enumValues, javaType, constValue);
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

    public @Nullable String getJavaType() {
        return javaType;
    }

    public @Nullable String getConstValue() {
        return constValue;
    }

    /**
     * Returns a new NormalSubschema that merges the given override fields into this one.
     * Used when resolving a $ref that has sibling fields (JSON Schema 2020-12 semantics:
     * the ref and siblings are all applied together). Override properties win on key conflict.
     */
    public NormalSubschema withMergedOverrides(
            @Nullable final Types overrideTypes,
            @Nullable final Properties overrideProperties,
            @Nullable final String overrideJavaType
    ) {
        final Types mergedTypes = overrideTypes != null ? overrideTypes : this.types;
        final Properties mergedProperties;
        if (overrideProperties != null && this.properties != null) {
            // Union of both property maps; override properties win on key conflict
            final java.util.LinkedHashMap<String, Subschema> merged = new java.util.LinkedHashMap<>(this.properties.getProperties());
            merged.putAll(overrideProperties.getProperties());
            mergedProperties = new Properties(merged);
        } else if (overrideProperties != null) {
            mergedProperties = overrideProperties;
        } else {
            mergedProperties = this.properties;
        }
        final String mergedJavaType = overrideJavaType == null ? this.javaType : overrideJavaType;
        return new NormalSubschema(isResolved, inSelfReference, reference, selfReferenceName,
                mergedTypes, mergedProperties, itemsType, enumValues, mergedJavaType, constValue);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof NormalSubschema subschema)) return false;
        return Objects.equals(types, subschema.types) && Objects.equals(properties, subschema.properties) && Objects.equals(itemsType, subschema.itemsType) && Objects.equals(enumValues, subschema.enumValues) && Objects.equals(javaType, subschema.javaType) && Objects.equals(constValue, subschema.constValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(types, properties, itemsType, enumValues, javaType, constValue);
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
        if (javaType != null) {
            fields.add("\"x-javaType\":\"" + javaType + "\"");
        }
        if (constValue != null) {
            fields.add("\"const\":\"" + constValue + "\"");
        }
        return "{" +
                (inSelfReference ? "*" : "") + // FIXME: Remove eventually, just marks the inSelfReference items
                String.join(",", fields) + "}";
    }
}
