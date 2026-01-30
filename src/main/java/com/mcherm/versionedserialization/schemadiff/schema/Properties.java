package com.mcherm.versionedserialization.schemadiff.schema;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/** Represents a properties list for an object. */
public class Properties {
    private final Map<String, Subschema> properties;

    /** Constructor */
    public Properties(Map<String, Subschema> properties) {
        this.properties = properties;
    }

    /** Getter */
    public Map<String, Subschema> getProperties() {
        return properties;
    }

    /** Returns true if all child Subschema are resolved. */
    public boolean allResolved() {
        return properties.values().stream().allMatch(Subschema::isResolved);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Properties that)) return false;
        return Objects.equals(properties, that.properties);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(properties);
    }

    @Override
    public String toString() {
        return "{" +
        properties.entrySet().stream()
                .map(entry -> "\"" + entry.getKey() + "\":" + entry.getValue().toString())
                .collect(Collectors.joining(",")) + "}";
    }
}
