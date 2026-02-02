package com.mcherm.versionedserialization.schemadiff.deltas;

import java.util.Objects;

/**
 * The abstract parent representing a specific change that needs to be made to a JSON
 * document when converting from one format to another.
 */
public sealed abstract class Delta permits Add, Drop, Change {
    private final String fieldName;

    public Delta(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFieldName() {
        return fieldName;
    }

    /**
     * Returns true if this delta requires the user to provide code to handle it,
     * false if it can be handled automatically (for instance, by using a default value).
     */
    public boolean requiresCustomization() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Delta that)) return false;
        return Objects.equals(fieldName, that.fieldName);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(fieldName);
    }
}
