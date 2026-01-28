package com.mcherm.versionedserialization.objects.contents;

import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/** A class that has a (nullable!) reference to itself. */
public class SelfNested {
    final private int value;
    @Nullable
    final private SelfNested next;

    /** No-args constructor; value defaults to 0. */
    public SelfNested() {
        this(0, null);
    }

    /** Constructor */
    public SelfNested(int value, SelfNested next) {
        this.value = value;
        this.next = next;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof SelfNested that)) return false;
        return value == that.value && Objects.equals(next, that.next);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, next);
    }

    public int getValue() {
        return value;
    }

    public SelfNested getNext() {
        return next;
    }
}
