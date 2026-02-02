package com.mcherm.versionedserialization.migration;

import com.fasterxml.jackson.databind.JsonNode;

public interface UpdateRule {
    JsonNode mapField(final UpdateContext updateContext, final String fieldToPopulate);
}
