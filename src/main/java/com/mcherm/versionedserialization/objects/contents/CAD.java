package com.mcherm.versionedserialization.objects.contents;

/** Represents Canadian currency. */
public final class CAD extends Currency {
    @Override
    public String currencyCode() {
        return "CAD";
    }
}
