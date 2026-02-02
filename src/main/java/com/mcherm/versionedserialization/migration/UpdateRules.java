package com.mcherm.versionedserialization.migration;

import com.mcherm.versionedserialization.schemadiff.SchemaDiffer;
import com.mcherm.versionedserialization.schemadiff.SchemaParser;
import com.mcherm.versionedserialization.schemadiff.UnsupportedSchemaFeature;
import com.mcherm.versionedserialization.schemadiff.deltas.Delta;
import com.mcherm.versionedserialization.schemadiff.deltas.SchemaDeltas;
import com.mcherm.versionedserialization.schemadiff.schema.SchemaInfo;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * A class containing a whole set of update rules telling how to migrate from one
 * schema to another.
 */
public class UpdateRules {
    private final Map<String,UpdateRule> updateRules;

    public UpdateRules(Map<String, UpdateRule> updateRules) {
        this.updateRules = updateRules;
    }

    public Optional<UpdateRule> getUpdateRule(String fieldName) {
        return Optional.ofNullable(updateRules.get(fieldName));
    }

    /**
     * This is used to check whether a set of rules is complete. It is given the
     * schemas for a source and target object and returns a list of any fields
     * that need rules. If the set of rules is complete it will return an empty
     * list.
     */
    public List<String> missingRules(String sourceSchema, String targetSchema) {
        final SchemaInfo sourceSchemaParsed;
        final SchemaInfo targetSchemaParsed;
        try {
            sourceSchemaParsed = SchemaParser.parse(sourceSchema);
            targetSchemaParsed = SchemaParser.parse(targetSchema);
        } catch (UnsupportedSchemaFeature err) {
            throw new RuntimeException("Schema had unsupported feature: " + err.getMessage());
        }
        final SchemaDeltas schemaDeltas = SchemaDiffer.diff(sourceSchemaParsed, targetSchemaParsed);
        return schemaDeltas.getDeltas().stream()
                .map(Delta::getFieldName)
                .filter(fieldName -> !updateRules.containsKey(fieldName))
                .toList();
    }
}
