package com.mcherm.versionedserialization.objects;

import java.util.Map;

/** A class with a Map field, to test schema handling of Map types. */
public class MappedV1 {
    public String name;
    public Map<String, String> metadata;
}
