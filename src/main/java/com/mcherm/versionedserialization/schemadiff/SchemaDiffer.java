package com.mcherm.versionedserialization.schemadiff;

import com.mcherm.versionedserialization.schemadiff.deltas.Add;
import com.mcherm.versionedserialization.schemadiff.deltas.Change;
import com.mcherm.versionedserialization.schemadiff.deltas.Drop;
import com.mcherm.versionedserialization.schemadiff.deltas.SchemaDeltas;
import com.mcherm.versionedserialization.schemadiff.schema.SchemaInfo;
import com.mcherm.versionedserialization.schemadiff.schema.Subschema;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/** Contains a function to compare two SchemaInfo objects and report on the differences. */
public class SchemaDiffer {

    public static SchemaDeltas diff(final SchemaInfo first, final SchemaInfo second) {
        final Map<String, Subschema> firstProperties = first.getProperties().getProperties();
        final Map<String, Subschema> secondProperties = second.getProperties().getProperties();

        final Set<String> allFields = new LinkedHashSet<>();
        allFields.addAll(firstProperties.keySet());
        allFields.addAll(secondProperties.keySet());

        final SchemaDeltas schemaDeltas = new SchemaDeltas();
        for (String field : allFields) {
            final Subschema firstSubschema = firstProperties.get(field);
            final Subschema secondSubschema = secondProperties.get(field);
            if (firstSubschema == null && secondSubschema == null) {
                throw new RuntimeException("impossible for both be null");
            } else if (firstSubschema != null && secondSubschema == null) {
                schemaDeltas.getDrops().add(new Drop(field, firstSubschema));
            } else if (firstSubschema == null && secondSubschema != null) {
                schemaDeltas.getAdds().add(new Add(field, secondSubschema));
            } else {
                if (!firstSubschema.equals(secondSubschema)) {
                    schemaDeltas.getChanges().add(new Change(field, firstSubschema, secondSubschema));
                }
            }
        }
        return schemaDeltas;
    }
}
