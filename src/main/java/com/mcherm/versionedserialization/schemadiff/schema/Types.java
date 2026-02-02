package com.mcherm.versionedserialization.schemadiff.schema;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/** In a JSON schema, this is a list of types a subschema can take on. */
public class Types {
    private final Set<PrimitiveType> types;

    public Types(Set<PrimitiveType> types) {
        this.types = types;
    }

    public Set<PrimitiveType> getTypes() {
        return types;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Types that)) return false;
        return Objects.equals(types, that.types);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(types);
    }

    @Override
    public String toString() {
        return "[" + types.stream().map(PrimitiveType::toString).collect(Collectors.joining(",")) + "]";
    }
}
