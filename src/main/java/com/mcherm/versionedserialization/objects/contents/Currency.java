package com.mcherm.versionedserialization.objects.contents;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.mcherm.versionedserialization.objects.contents.serialize.CurrencyDeserializer;
import com.mcherm.versionedserialization.objects.contents.serialize.CurrencySerializer;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Parent class for currency types. */
@JsonSerialize(using = CurrencySerializer.class)
@JsonDeserialize(using = CurrencyDeserializer.class)
public abstract sealed class Currency permits USD, CAD {

    static final Map<String,Currency> CURRENCY_MAP;

    static {
        final List<Currency> currencies = List.of(new USD(), new CAD());
        final Map<String,Currency> currencyLookup = new LinkedHashMap<>();
        for (final Currency currency : currencies) {
            currencyLookup.put(currency.currencyCode(), currency);
        }
        CURRENCY_MAP = Collections.unmodifiableMap(currencyLookup);
    }

    /** Factory function to create from a currency code string. Invalid values throw IllegalArgumentException. */
    public static Currency fromString(final String currencyCode) {
        final Currency result = CURRENCY_MAP.get(currencyCode);
        if (result == null) {
            throw new IllegalArgumentException("Unsupported currency: \"" + currencyCode + "\"");
        }
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Currency other)) return false;
        return this.currencyCode().equals(other.currencyCode());
    }

    @Override
    public int hashCode() {
        return currencyCode().hashCode();
    }

    @Override
    public String toString() {
        return currencyCode();
    }

    /** Return a string representing the currency code. Never null. */
    public abstract String currencyCode();

}
