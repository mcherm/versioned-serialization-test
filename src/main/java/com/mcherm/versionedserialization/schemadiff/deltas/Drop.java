package com.mcherm.versionedserialization.schemadiff.deltas;

import com.mcherm.versionedserialization.schemadiff.schema.Subschema;

import java.util.Objects;

/** Represents dropping an existing field. */
public final class Drop extends Alteration {
    private final Subschema subschema;

    public Drop(String fieldName, Subschema subschema) {
        super(fieldName);
        this.subschema = subschema;
    }

    public Subschema getSubschema() {
        return subschema;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Drop drop)) return false;
        return super.equals(o) && Objects.equals(subschema, drop.subschema);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), subschema);
    }

    @Override
    public String toString() {
        return "Drop " + getFieldName() + " of type " + subschema;
    }
}
