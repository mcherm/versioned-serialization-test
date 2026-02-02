package com.mcherm.versionedserialization.schemadiff;

import com.fasterxml.jackson.databind.JsonNode;
import com.mcherm.versionedserialization.SerializationUtil;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
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
     * Find the default (if any) to use for a given Java class. Primitive types are
     * handled by mapping them to their corresponding wrapper classes.
     *
     * @param clazz the class to be mapped
     * @return an Optional that wraps JsonNode that can be used as a default, or Empty if no default applies
     */
    public static Optional<JsonNode> getDefault(final Class<?> clazz) {
        final Class<?> lookupClass = clazz.isPrimitive()
                ? primitiveToWrapper(clazz)
                : clazz;
        for (DefaultingClass entry : defaults) {
            if (entry.clazz.isAssignableFrom(lookupClass)) {
                return Optional.of(entry.defaultValue);
            }
        }
        return Optional.empty();
    }

    /**
     * Maps a primitive class (e.g. {@code int.class}) to its wrapper class (e.g. {@code Integer.class}).
     */
    private static Class<?> primitiveToWrapper(final Class<?> primitiveClass) {
        if (primitiveClass == boolean.class) return Boolean.class;
        if (primitiveClass == byte.class) return Byte.class;
        if (primitiveClass == short.class) return Short.class;
        if (primitiveClass == int.class) return Integer.class;
        if (primitiveClass == long.class) return Long.class;
        if (primitiveClass == float.class) return Float.class;
        if (primitiveClass == double.class) return Double.class;
        if (primitiveClass == char.class) return Character.class;
        return primitiveClass;
    }

    private static final Map<String, Class<?>> PRIMITIVE_TYPES_BY_NAME = Map.of(
            "boolean", boolean.class,
            "byte", byte.class,
            "short", short.class,
            "int", int.class,
            "long", long.class,
            "float", float.class,
            "double", double.class,
            "char", char.class
    );

    /**
     * Find the default (if any) to use for a given Java class. If passed null instead of a valid string, this
     * returns Empty.
     *
     * @param classString a string naming the class to be mapped (may include generic parameters like "java.util.List&lt;Widget&gt;")
     * @return an Optional that wraps JsonNode that can be used as a default, or Empty if no default applies
     */
    public static Optional<JsonNode> getDefault(@Nullable final String classString) {
        if (classString == null) {
            return Optional.empty();
        }
        try {
            // Strip generic parameters (e.g., "java.util.List<Widget>" -> "java.util.List")
            final int genericStart = classString.indexOf('<');
            final String className = genericStart == -1 ? classString : classString.substring(0, genericStart);
            // Check for primitive type names like "boolean", "int", etc. since
            // Class.forName doesn't handle them.
            Class<?> clazz = PRIMITIVE_TYPES_BY_NAME.get(className);
            if (clazz == null) {
                clazz = Class.forName(className);
            }
            return getDefault(clazz);
        } catch (ClassNotFoundException e) {
            return Optional.empty();
        }
    }

}
