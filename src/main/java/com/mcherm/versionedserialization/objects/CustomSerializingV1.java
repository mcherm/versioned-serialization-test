package com.mcherm.versionedserialization.objects;

import java.util.Optional;

/**
 * A class demonstrating a number of different ways to customize the
 * jackson serialization.
 */
public class CustomSerializingV1 {
    public String simplePublic;
    private final String simpleGetter;
    private final Optional<String> optionalValue;

    /**
     * Private constructor for use by deserialization tools only. This leaves the
     * object in an invalid state.
     */
    private CustomSerializingV1() {
        this.simpleGetter = null;
        this.optionalValue = null;
    }

    /** All-fields constructor. */
    public CustomSerializingV1(String simplePublic, String simpleGetter, Optional<String> optionalValue) {
        this.simplePublic = simplePublic;
        this.simpleGetter = simpleGetter;
        this.optionalValue = optionalValue;
    }

    /** Getter for private field. */
    public String getSimpleGetter() {
        return simpleGetter;
    }

    /** Getter for a field. */
    public Optional<String> getOptionalValue() {
        return optionalValue;
    }

}
