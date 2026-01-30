package com.mcherm.versionedserialization.schemadiff;

import com.fasterxml.jackson.databind.JsonNode;
import com.mcherm.versionedserialization.SerializationUtil;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * This maintains a list of known classes that have a sensible default to use, and
 * can return the JSON to use for that default.
 */
public class DefaultableClasses {

    private record DefaultingClass(Class<?> clazz, JsonNode defaultValue) {}
    private record DefaultValueSpec(Class<?> clazz, String jsonString) {}

    private static final List<DefaultingClass> defaults;

    static {
        final List<DefaultValueSpec> defaultValues = List.of(
                new DefaultValueSpec(Boolean.class, "false"),
                new DefaultValueSpec(Byte.class, "0"),
                new DefaultValueSpec(Short.class, "0"),
                new DefaultValueSpec(Integer.class, "0"),
                new DefaultValueSpec(Long.class, "0"),
                new DefaultValueSpec(Float.class, "0.0"),
                new DefaultValueSpec(Double.class, "0.0"),
                new DefaultValueSpec(String.class, "\"\""),
                new DefaultValueSpec(Optional.class, "null"),
                new DefaultValueSpec(List.class, "[]"),
                new DefaultValueSpec(Set.class, "[]")
        );
        defaults = defaultValues.stream()
                .map(spec -> new DefaultingClass(
                        spec.clazz,
                        SerializationUtil.deserializeAsNode(spec.jsonString)
                ))
                .toList();
    }

    /**
     * Find the default (if any) to use for a given Java class.
     *
     * @param clazz the class to be mapped
     * @return an Optional that wraps JsonNode that can be used as a default, or Empty if no default applies
     */
    public static Optional<JsonNode> getDefault(final Class<?> clazz) {
        for (DefaultingClass entry : defaults) {
            if (entry.clazz.isAssignableFrom(clazz)) {
                return Optional.of(entry.defaultValue);
            }
        }
        return Optional.empty();
    }

}
