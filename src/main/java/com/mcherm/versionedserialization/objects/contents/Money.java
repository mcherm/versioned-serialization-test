package com.mcherm.versionedserialization.objects.contents;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.Objects;

public class Money<Cur extends Currency> {
    private final BigDecimal amount;
    private final Cur currency;

    /** Constructor */
    private Money(final BigDecimal amount, final Cur currency) {
        this.amount = amount;
        this.currency = currency;
    }

    /** Factory function. */
    public static <Cur extends Currency> Money<Cur> fromBD(
            final BigDecimal amount,
            final Cur currency
    ) {
        return new Money<>(amount, currency);
    }

    /** Factory function. */
    public static <Cur extends Currency> Money<Cur> fromString(
            final String amount,
            final Cur currency
    ) {
        return new Money<>(new BigDecimal(amount), currency);
    }

    /** Factory function. */
    @JsonCreator
    public static <Cur extends Currency> Money<Cur> fromString(
            @JsonProperty("amount") final String amount,
            @JsonProperty("currency") final String currencyCode
    ) {
        return new Money(new BigDecimal(amount), Currency.fromString(currencyCode));
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Money<?> money)) return false;
        return Objects.equals(amount, money.amount) && Objects.equals(currency, money.currency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount, currency);
    }

    @Override
    public String toString() {
        return "$" + amount + " " + currency;
    }

    /** Getter */
    public BigDecimal getAmount() {
        return amount;
    }

    /** Getter */
    public Cur getCurrency() {
        return currency;
    }

}
