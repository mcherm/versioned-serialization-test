package com.mcherm.versionedserialization.objects;

import com.mcherm.versionedserialization.objects.contents.EmployeeV2;

import java.util.Map;

/** Like MappedV2b but the map value type is EmployeeV2 (which adds a "title" field). */
public class MappedV2c {
    public String name;
    public Map<String, EmployeeV2> metadata;
}
