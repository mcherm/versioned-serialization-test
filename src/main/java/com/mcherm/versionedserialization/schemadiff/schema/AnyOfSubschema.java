package com.mcherm.versionedserialization.schemadiff.schema;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * A subschema representing an anyOf node: a set of alternative subschemas, exactly
 * one of which applies. Used for polymorphic types where each alternative represents
 * a different subtype.
 */
public final class AnyOfSubschema implements Subschema {
    private final List<Subschema> options;
    @Nullable
    private final String javaType;

    public AnyOfSubschema(final List<Subschema> options, final @Nullable String javaType) {
        this.options = List.copyOf(options);
        this.javaType = javaType;
    }

    public List<Subschema> getOptions() {
        return options;
    }

    public @Nullable String getJavaType() {
        return javaType;
    }

    @Override
    public boolean isResolved() {
        return options.stream().allMatch(Subschema::isResolved);
    }

    @Override
    public boolean isInSelfReference() {
        return options.stream().anyMatch(Subschema::isInSelfReference);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof AnyOfSubschema that)) return false;
        return Objects.equals(options, that.options) && Objects.equals(javaType, that.javaType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(options, javaType);
    }

    @Override
    public String toString() {
        final String optionsStr = options.stream()
                .map(Object::toString)
                .collect(Collectors.joining(","));
        final String javaTypePart = javaType != null
                ? ",\"x-javaType\":\"" + javaType + "\""
                : "";
        return "{\"anyOf\":[" + optionsStr + "]" + javaTypePart + "}";
    }
}
