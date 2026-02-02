package com.mcherm.versionedserialization.objects;

import com.mcherm.versionedserialization.objects.contents.EmployeeV1;

import java.util.Map;

/** A class with a Map whose values are EmployeeV1 objects. */
public class MappedV2b {
    public String name;
    public Map<String, EmployeeV1> metadata;
}
