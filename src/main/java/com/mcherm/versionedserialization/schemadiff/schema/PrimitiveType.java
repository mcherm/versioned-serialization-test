package com.mcherm.versionedserialization.schemadiff.schema;

/** Enum for the types of content in a JSON document. */
public enum PrimitiveType {
    OBJECT, ARRAY, STRING, NUMBER, INTEGER, BOOLEAN, NULL;

    @Override
    public String toString() {
        return "\"" + name().toLowerCase() + "\"";
    }
}
