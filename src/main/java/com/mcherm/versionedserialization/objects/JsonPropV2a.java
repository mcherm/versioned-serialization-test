package com.mcherm.versionedserialization.objects;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Adding a @JsonProperty-annotated "phoneNumber" field to JsonPropV1. */
public class JsonPropV2a {
    @JsonProperty("first_name")
    public String firstName;

    @JsonProperty("last_name")
    public String lastName;

    public int age;

    @JsonProperty("phone_number")
    public String phoneNumber;
}
