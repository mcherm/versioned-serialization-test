package com.mcherm.versionedserialization.schemadiff.schema;

import java.util.Objects;

/**
 * A subschema representing a self-reference: a reference to something higher up in the tree.
 */
public final class SelfReference implements Subschema {

    /** Factory Function */
    public static SelfReference fromSelfReference(
            final String selfReferenceName
    ) {
        return new SelfReference(selfReferenceName);
    }

    private final String selfReferenceName; // if non-null everything else is null

    public SelfReference(String selfReferenceName) {
        this.selfReferenceName = selfReferenceName;
    }

    @Override
    public boolean isResolved() {
        return true;
    }

    @Override
    public boolean isInSelfReference() {
        return true;
    }

    public String getSelfReferenceName() {
        return selfReferenceName;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof SelfReference that)) return false;
        return Objects.equals(selfReferenceName, that.selfReferenceName);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(selfReferenceName);
    }

    @Override
    public String toString() {
        return "{\"$selfRef\":\"" + selfReferenceName + "\"}";
    }
}
