package com.mcherm.versionedserialization.schemadiff.deltas;

import com.fasterxml.jackson.databind.JsonNode;
import com.mcherm.versionedserialization.schemadiff.schema.Subschema;

import java.util.Objects;

/** Represents adding a new field which has a sensible default value. */
public final class DefaultingAdd extends Add {
    private final JsonNode defaultValue;

    public DefaultingAdd(String fieldName, Subschema subschema, JsonNode defaultValue) {
        super(fieldName, subschema);
        this.defaultValue = defaultValue;
    }

    public JsonNode getDefaultValue() {
        return defaultValue;
    }

    @Override
    public boolean requiresCustomization() {
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof DefaultingAdd that)) return false;
        return super.equals(o) && Objects.equals(defaultValue, that.defaultValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), defaultValue);
    }

    @Override
    public String toString() {
        return "Add " + getFieldName() + " of type " + getSubschema() + " defaulting to " + defaultValue;
    }
}
