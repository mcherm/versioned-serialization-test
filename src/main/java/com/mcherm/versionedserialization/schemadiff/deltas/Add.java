package com.mcherm.versionedserialization.schemadiff.deltas;

import com.mcherm.versionedserialization.schemadiff.schema.Subschema;

import java.util.Objects;

/** Represents adding a new field. */
public sealed abstract class Add extends Delta permits CustomAdd, DefaultingAdd {
    private final Subschema subschema;

    public Add(String fieldName, Subschema subschema) {
        super(fieldName);
        this.subschema = subschema;
    }

    public Subschema getSubschema() {
        return subschema;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Add add)) return false;
        return super.equals(o) && Objects.equals(subschema, add.subschema);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), subschema);
    }
}
