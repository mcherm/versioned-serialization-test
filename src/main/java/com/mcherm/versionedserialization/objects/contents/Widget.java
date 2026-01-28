package com.mcherm.versionedserialization.objects.contents;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class Widget {
    private final String name;
    private final int shoeSize;

    /** Constructor. */
    @JsonCreator
    public Widget(
            @JsonProperty("name") final String name,
            @JsonProperty("shoeSize") final int shoeSize
    ) {
        this.name = name;
        this.shoeSize = shoeSize;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Widget widget)) return false;
        return shoeSize == widget.shoeSize && Objects.equals(name, widget.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, shoeSize);
    }

    public String getName() {
        return name;
    }

    public int getShoeSize() {
        return shoeSize;
    }
}
