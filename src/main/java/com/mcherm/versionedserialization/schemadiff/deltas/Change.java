package com.mcherm.versionedserialization.schemadiff.deltas;

import com.mcherm.versionedserialization.schemadiff.schema.Subschema;

import java.util.Objects;

/** Represents a change to a field. */
public final class Change implements Alteration {
    private final String fieldName;
    private final Subschema startingSubschema;
    private final Subschema endingSubschema;

    public Change(String fieldName, Subschema startingSubschema, Subschema endingSubschema) {
        this.fieldName = fieldName;
        this.startingSubschema = startingSubschema;
        this.endingSubschema = endingSubschema;
    }

    public String getFieldName() {
        return fieldName;
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
        return Objects.equals(fieldName, change.fieldName) && Objects.equals(startingSubschema, change.startingSubschema) && Objects.equals(endingSubschema, change.endingSubschema);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fieldName, startingSubschema, endingSubschema);
    }

    @Override
    public String toString() {
        return "Change " + fieldName + " from type " + startingSubschema + " to type " + endingSubschema;
    }

}
