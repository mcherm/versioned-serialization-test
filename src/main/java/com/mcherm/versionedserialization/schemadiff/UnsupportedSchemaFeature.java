package com.mcherm.versionedserialization.schemadiff;

/**
 * Exception thrown when trying to parse a schema and it uses a feature we
 * don't yet support.
 */
public class UnsupportedSchemaFeature extends Exception {
    public UnsupportedSchemaFeature(String message) {
        super(message);
    }
}
