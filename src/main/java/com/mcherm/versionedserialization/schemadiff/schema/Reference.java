package com.mcherm.versionedserialization.schemadiff.schema;

import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/** Represents a reference. May optionally carry override fields from sibling keywords. */
public final class Reference implements Subschema {
    /** This is JUST the part after the "#/$defs/" and no other prefix is supported. */
    private final String name;
    @Nullable
    private final String javaType;
    @Nullable
    private final Types overrideTypes;
    @Nullable
    private final Properties overrideProperties;

    public Reference(String name) {
        this(name, null, null, null);
    }

    public Reference(String name, @Nullable String javaType) {
        this(name, javaType, null, null);
    }

    public Reference(String name, @Nullable String javaType, @Nullable Types overrideTypes, @Nullable Properties overrideProperties) {
        this.name = name;
        this.javaType = javaType;
        this.overrideTypes = overrideTypes;
        this.overrideProperties = overrideProperties;
    }

    public String getName() {
        return name;
    }

    public @Nullable String getJavaType() {
        return javaType;
    }

    public @Nullable Types getOverrideTypes() {
        return overrideTypes;
    }

    public @Nullable Properties getOverrideProperties() {
        return overrideProperties;
    }

    public boolean hasOverrides() {
        return overrideTypes != null || overrideProperties != null;
    }

    @Override
    public boolean isResolved() {
        return false;
    }

    @Override
    public boolean isInSelfReference() {
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Reference reference)) return false;
        return Objects.equals(name, reference.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }

    @Override
    public String toString() {
        return "{\"$ref\":\"" + name + "\"}";
    }
}
