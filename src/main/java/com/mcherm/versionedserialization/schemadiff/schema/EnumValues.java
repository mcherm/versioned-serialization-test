package com.mcherm.versionedserialization.schemadiff.schema;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/** Represents a list of valid enum values. For now, we ONLY support a list of Strings. */
public class EnumValues {
    private final List<String> values;

    public EnumValues(List<String> values) {
        this.values = values;
    }

    public List<String> getValues() {
        return values;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof EnumValues that)) return false;
        return Objects.equals(values, that.values);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(values);
    }

    @Override
    public String toString() {
        return "[" + values.stream().map(x->"\"" + x + "\"").collect(Collectors.joining(",")) + "]";
    }
}
