package com.mcherm.versionedserialization.migration;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * This interface is what an instrument author provides to document how to populate a particular
 * field. It is passed an UpdateContext, which contains the full source document as well as
 * source and destination schemas, and the name of the field to populate. It returns the JSON
 * to use for that particular field.
 */
public interface UpdateRule {
    JsonNode mapField(final UpdateContext updateContext, final String fieldToPopulate);
}
