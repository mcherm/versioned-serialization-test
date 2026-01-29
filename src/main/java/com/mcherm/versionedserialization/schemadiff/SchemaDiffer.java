package com.mcherm.versionedserialization.schemadiff;

import com.mcherm.versionedserialization.schemadiff.deltas.Add;
import com.mcherm.versionedserialization.schemadiff.deltas.Change;
import com.mcherm.versionedserialization.schemadiff.deltas.Drop;
import com.mcherm.versionedserialization.schemadiff.deltas.SchemaDeltas;
import com.mcherm.versionedserialization.schemadiff.schema.Properties;
import com.mcherm.versionedserialization.schemadiff.schema.SchemaInfo;
import com.mcherm.versionedserialization.schemadiff.schema.Subschema;

import java.util.LinkedHashSet;
import java.util.Map;
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
            final Subschema firstSubschema = firstProperties.get(field);
            final Subschema secondSubschema = secondProperties.get(field);
            if (firstSubschema == null && secondSubschema == null) {
                throw new RuntimeException("impossible for both be null");
            } else if (firstSubschema != null && secondSubschema == null) {
                schemaDeltas.getDrops().add(new Drop(prefix + field, firstSubschema));
            } else if (firstSubschema == null && secondSubschema != null) {
                schemaDeltas.getAdds().add(new Add(prefix + field, secondSubschema));
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
                    final Subschema firstItems = firstSubschema.getItemsType();
                    final Subschema secondItems = secondSubschema.getItemsType();
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
                    schemaDeltas.getChanges().add(new Change(prefix + field, firstSubschema, secondSubschema));

                }
            }
        }
    }

}
