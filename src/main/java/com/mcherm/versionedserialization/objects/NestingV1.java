package com.mcherm.versionedserialization.objects;

import com.mcherm.versionedserialization.objects.contents.SelfNested;
import org.jetbrains.annotations.Nullable;

/** A class which contains fields that contain themselves to catch infinite regress issues. */
public class NestingV1 {
    @Nullable
    private SelfNested nestlings;

    /** No-args constructor. */
    public NestingV1() {}

    /** Constructor taking a SelfNested. */
    public NestingV1(@Nullable SelfNested nestlings) {
        this.nestlings = nestlings;
    }

    public @Nullable SelfNested getNestlings() {
        return nestlings;
    }
}
