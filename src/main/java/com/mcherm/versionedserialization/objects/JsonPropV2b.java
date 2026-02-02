package com.mcherm.versionedserialization.objects;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Removing the "lastName" / "last_name" field from JsonPropV1. */
public class JsonPropV2b {
    @JsonProperty("first_name")
    public String firstName;

    public int age;
}
