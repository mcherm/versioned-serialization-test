package com.mcherm.versionedserialization.schemadiff.deltas;

import com.mcherm.versionedserialization.schemadiff.schema.Subschema;

import java.util.Objects;

/** Represents a change to a field. */
public final class Change extends Alteration {
    private final Subschema startingSubschema;
    private final Subschema endingSubschema;

    public Change(String fieldName, Subschema startingSubschema, Subschema endingSubschema) {
        super(fieldName);
        this.startingSubschema = startingSubschema;
        this.endingSubschema = endingSubschema;
    }

    public Subschema getStartingSubschema() {
        return startingSubschema;
    }

    public Subschema getEndingSubschema() {
        return endingSubschema;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Change change)) return false;
        return super.equals(o) && Objects.equals(startingSubschema, change.startingSubschema) && Objects.equals(endingSubschema, change.endingSubschema);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), startingSubschema, endingSubschema);
    }

    @Override
    public String toString() {
        return "Change " + getFieldName() + " from type " + startingSubschema + " to type " + endingSubschema;
    }

}
