package com.mcherm.versionedserialization.schemadiff.schema;


/**
 * A subschema... what things can be defined to be. We are applying some limitations,
 * like that subschemas don't have their own defs section.
 */
public sealed interface Subschema permits NormalSubschema, Reference, SelfReference, AnyOfSubschema {

    boolean isResolved();

    boolean isInSelfReference();

}
