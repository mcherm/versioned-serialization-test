package com.mcherm.versionedserialization.schemadiff.schema;

public enum PrimitiveType {
    OBJECT, ARRAY, STRING, NUMBER, INTEGER, BOOLEAN, NULL;

    @Override
    public String toString() {
        return "\"" + name().toLowerCase() + "\"";
    }
}
