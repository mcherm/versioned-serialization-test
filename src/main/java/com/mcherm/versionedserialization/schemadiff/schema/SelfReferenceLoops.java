package com.mcherm.versionedserialization.schemadiff.schema;

import java.util.List;
import java.util.Set;

// FIXME: Remove this
/** The information we can obtain from a Subschema about what loops they participate in. */
public class SelfReferenceLoops {
    private final Set<List<String>> loops;

    public SelfReferenceLoops(Set<List<String>> loops) {
        this.loops = loops;
    }

    public Set<List<String>> getLoops() {
        return loops;
    }
}
