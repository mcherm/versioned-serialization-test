package com.mcherm.versionedserialization.objects.contents;

/** Represents US currency. */
public final class USD extends Currency {
    @Override
    public String currencyCode() {
        return "USD";
    }
}
