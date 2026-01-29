package com.mcherm.versionedserialization.schemadiff.deltas;

import com.mcherm.versionedserialization.schemadiff.schema.Subschema;

import java.util.Objects;

/** Represents adding a new field. */
public final class Add implements Alteration {
    private final String fieldName;
    private final Subschema subschema;

    public Add(String fieldName, Subschema subschema) {
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
        if (!(o instanceof Add add)) return false;
        return Objects.equals(fieldName, add.fieldName) && Objects.equals(subschema, add.subschema);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fieldName, subschema);
    }

    @Override
    public String toString() {
        return "Add " + fieldName + " of type " + subschema;
    }
}
