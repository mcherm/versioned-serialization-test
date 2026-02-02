package com.mcherm.versionedserialization.objects;

import com.fasterxml.jackson.annotation.JsonProperty;

/** A class using @JsonProperty to rename fields in the JSON output. */
public class JsonPropV1 {
    @JsonProperty("first_name")
    public String firstName;

    @JsonProperty("last_name")
    public String lastName;

    public int age;
}
