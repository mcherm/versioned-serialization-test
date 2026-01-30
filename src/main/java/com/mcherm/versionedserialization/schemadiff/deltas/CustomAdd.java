package com.mcherm.versionedserialization.schemadiff.deltas;

import com.mcherm.versionedserialization.schemadiff.schema.Subschema;

/** Represents adding a new field which does not have a sensible default value. */
public final class CustomAdd extends Add {

    public CustomAdd(String fieldName, Subschema subschema) {
        super(fieldName, subschema);
    }

    @Override
    public String toString() {
        return "Add " + getFieldName() + " of type " + getSubschema();
    }
}
