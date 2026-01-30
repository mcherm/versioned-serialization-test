package com.mcherm.versionedserialization.schemadiff.schema;

import java.util.Objects;

/** Represents a reference. */
public final class Reference implements Subschema {
    /** This is JUST the part after the "#/$defs/" and no other prefix is supported. */
    private final String name;

    public Reference(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
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
