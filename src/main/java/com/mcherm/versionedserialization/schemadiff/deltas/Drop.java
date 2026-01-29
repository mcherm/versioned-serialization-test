package com.mcherm.versionedserialization.schemadiff.deltas;

import com.mcherm.versionedserialization.schemadiff.schema.Subschema;

import java.util.Objects;

/** Represents dropping an existing field. */
public final class Drop implements Alteration {
    private final String fieldName;
    private final Subschema subschema;

    public Drop(String fieldName, Subschema subschema) {
        this.fieldName = fieldName;
        this.subschema = subschema;
    }

    public String getFieldName() {
        return fieldName;
    }

    public Subschema getSubschema() {
        return subschema;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Drop drop)) return false;
        return Objects.equals(fieldName, drop.fieldName) && Objects.equals(subschema, drop.subschema);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fieldName, subschema);
    }

    @Override
    public String toString() {
        return "Drop " + fieldName + " of type " + subschema;
    }
}
