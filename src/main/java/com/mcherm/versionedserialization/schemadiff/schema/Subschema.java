package com.mcherm.versionedserialization.schemadiff.schema;


/**
 * A subschema... what things can be defined to be. We are applying some limitations,
 * like that subschemas don't have their own defs section.
 */
// FIXME: Doesn't handle allOf, anyOf, oneOf
public sealed interface Subschema permits NormalSubschema, Reference, SelfReference {

    boolean isResolved();

    boolean isInSelfReference();

}
