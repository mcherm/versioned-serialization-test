package com.mcherm.versionedserialization.schemadiff.deltas;

import java.util.Objects;

public sealed abstract class Alteration permits Add, Drop, Change {
    private final String fieldName;

    public Alteration(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFieldName() {
        return fieldName;
    }

    /**
     * Returns true if this alteration requires the user to provide code to handle it,
     * false if it can be handled automatically (for instance, by using a default value).
     */
    public boolean requiresCustomization() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Alteration that)) return false;
        return Objects.equals(fieldName, that.fieldName);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(fieldName);
    }
}
