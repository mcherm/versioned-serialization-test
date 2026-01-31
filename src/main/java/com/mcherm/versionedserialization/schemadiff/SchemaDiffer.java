package com.mcherm.versionedserialization.schemadiff;

import com.fasterxml.jackson.databind.JsonNode;
import com.mcherm.versionedserialization.schemadiff.deltas.Add;
import com.mcherm.versionedserialization.schemadiff.deltas.Change;
import com.mcherm.versionedserialization.schemadiff.deltas.CustomAdd;
import com.mcherm.versionedserialization.schemadiff.deltas.DefaultingAdd;
import com.mcherm.versionedserialization.schemadiff.deltas.Drop;
import com.mcherm.versionedserialization.schemadiff.deltas.SchemaDeltas;
import com.mcherm.versionedserialization.schemadiff.schema.NormalSubschema;
import com.mcherm.versionedserialization.schemadiff.schema.Properties;
import com.mcherm.versionedserialization.schemadiff.schema.SchemaInfo;
import com.mcherm.versionedserialization.schemadiff.schema.Subschema;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/** Contains a function to compare two SchemaInfo objects and report on the differences. */
public class SchemaDiffer {

    public static SchemaDeltas diff(final SchemaInfo first, final SchemaInfo second) {
        final SchemaDeltas schemaDeltas = new SchemaDeltas();
        diffProperties(schemaDeltas, "", first.getProperties(), second.getProperties());
        return schemaDeltas;
    }

    /**
     * Compare two Properties and find the differences between them.
     *
     * @param schemaDeltas the SchemaDeltas being built up. THIS GETS MODIFIED.
     * @param prefix a prefix to the names (used when we recurse)
     * @param first the Properties from the first schema
     * @param second the Properties from the second schema
     */
    private static void diffProperties(
            final SchemaDeltas schemaDeltas,
            final String prefix,
            final Properties first,
            final Properties second
    ) {
        final Map<String, Subschema> firstProperties = first.getProperties();
        final Map<String, Subschema> secondProperties = second.getProperties();

        final Set<String> allFields = new LinkedHashSet<>();
        allFields.addAll(firstProperties.keySet());
        allFields.addAll(secondProperties.keySet());

        for (String field : allFields) {
            // FIXME: I am SOMEWHAT confident that these will always be a NormalSubschema, but
            //   really I should verify that holds true in all circumstances or handle it differently.
            final NormalSubschema firstSubschema = (NormalSubschema) firstProperties.get(field);
            final NormalSubschema secondSubschema = (NormalSubschema)  secondProperties.get(field);
            if (firstSubschema == null && secondSubschema == null) {
                throw new RuntimeException("impossible for both be null");
            } else if (firstSubschema != null && secondSubschema == null) {
                schemaDeltas.addAlteration(new Drop(prefix + field, firstSubschema));
            } else if (firstSubschema == null && secondSubschema != null) {
                // --- It's an Add, but which type? ---
                final Optional<JsonNode> defaultValue = DefaultableClasses.getDefault(secondSubschema.getJavaType());
                final Add add = defaultValue.isPresent()
                        ? new DefaultingAdd(prefix + field, secondSubschema, defaultValue.get())
                        : new CustomAdd(prefix + field, secondSubschema);
                schemaDeltas.addAlteration(add);
            } else {
                if (!firstSubschema.equals(secondSubschema)) {
                    // --- First, check for two inner records that changed ---
                    final Properties firstProps = firstSubschema.getProperties();
                    final Properties secondProps = secondSubschema.getProperties();
                    if (firstProps != null && secondProps != null) {
                        // They both have properties; we can do inner changes
                        final String newPrefix = prefix + field + "/";
                        diffProperties(schemaDeltas, newPrefix, firstProps, secondProps);
                        continue;
                    }
                    // --- Then check for two lists of a single type that changed ---
                    final NormalSubschema firstItems = (NormalSubschema) firstSubschema.getItemsType();
                    final NormalSubschema secondItems = (NormalSubschema) secondSubschema.getItemsType();
                    if (firstItems != null && secondItems != null) {
                        // they both have items... are both of those objects with properties?
                        final Properties firstItemsProps = firstItems.getProperties();
                        final Properties secondItemsProps = secondItems.getProperties();
                        if (firstItemsProps != null && secondItemsProps != null) {
                            // They both have properties; we can do inner changes
                            final String newPrefix = prefix + field + "[]";
                            diffProperties(schemaDeltas, newPrefix, firstItemsProps, secondItemsProps);
                            continue;
                        }
                    }
                    // --- Can't really do inner changes; report the change on this level ---
                    schemaDeltas.addAlteration(new Change(prefix + field, firstSubschema, secondSubschema));
                }
            }
        }
    }

}
