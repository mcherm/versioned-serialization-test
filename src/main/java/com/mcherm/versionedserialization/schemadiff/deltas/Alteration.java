package com.mcherm.versionedserialization.schemadiff.deltas;

public sealed interface Alteration permits Add, Drop, Change {
}
