package com.mcherm.versionedserialization.migration;

import java.util.Map;
import java.util.Optional;

public class UpdateRules {
    private final Map<String,UpdateRule> updateRules;

    public UpdateRules(Map<String, UpdateRule> updateRules) {
        this.updateRules = updateRules;
    }

    public Optional<UpdateRule> getUpdateRule(String fieldName) {
        return Optional.ofNullable(updateRules.get(fieldName));
    }
}
