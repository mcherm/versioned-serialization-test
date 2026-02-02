# versioned-serialization-test
A proof-of-concept demonstrating how we can serialize and deserialize different versions of a complex object.

## Problem Statement
We may have code which uses Jackson serialization to write out JSON serialized versions of some Java object, perhaps
arbitrarily complex Java objects. Later, we might re-write the code so the Java objects in question are changed in some
ways: adding some fields, altering others, perhaps in complex ways. The altered code might want to deserialize the
previously-written objects, but this wouldn't work because the format is now different.

This proof-of-concept demonstrates a way to address that. It can produce a schema (in JSON Schema format) defining what
form the current serialized JSON files take. When a new version of the object is created we can generate that schema
as well. We can compare the two schemas to find out the differences between them. Then we can make changes to the
original serialized file to produce a new one corresponding to the new format, after which we can read in the
transformed JSON to the new objects.

Some of the changes to be made can be automatically derived, for instance if a field is dropped we can simply delete
it, or if a new field is added we may be able to use a default value. Other changes might be arbitrarily complex
(perhaps what used to be 2 fields are combined into one using some logic). So we will allow the user to define logic
for populating new fields (operating on the entire JSON tree of the existing data), but not require it in cases where
a sensible default behavior can be derived.

## Implementation
Here is an overview of the key classes:
### SerializationUtil
Contains functions for serializing and deserializing JSON and for generating schemas.
### SchemaParser
Parses a JSON schema produced from <code>SerializationUtil</code> into a format defined in the
<code>com.mcherm.versionedserialization.schemadiff.schema</code> package.
### SchemaDiffer
Compares two schema from SchemaParser to find the "diffs" that need to be applied to migrate from one to the other,
which is returned in the form of a <code>SchemaDeltas</code> object.
### UpdateRules
Contains a set of UpdateRule objects specifying how to populate some of the fields in the new object. Is also able to
check whether a set of rules contains everything required to fully perform a migration from one format to another.
### Migrator
Converts an old format document to a new format given the schemas for both and a set of <code>UpdateRules</code>.