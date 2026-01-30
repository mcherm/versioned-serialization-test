package com.mcherm.versionedserialization.schemadiff.schema;

import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/** Represents a reference. */
public final class Reference implements Subschema {
    /** This is JUST the part after the "#/$defs/" and no other prefix is supported. */
    private final String name;
    @Nullable
    private final String javaType;

    public Reference(String name) {
        this(name, null);
    }

    public Reference(String name, @Nullable String javaType) {
        this.name = name;
        this.javaType = javaType;
    }

    public String getName() {
        return name;
    }

    public @Nullable String getJavaType() {
        return javaType;
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
