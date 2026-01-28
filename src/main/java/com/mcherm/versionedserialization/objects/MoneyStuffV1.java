package com.mcherm.versionedserialization.objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.mcherm.versionedserialization.objects.contents.CAD;
import com.mcherm.versionedserialization.objects.contents.Currency;
import com.mcherm.versionedserialization.objects.contents.Money;
import com.mcherm.versionedserialization.objects.contents.USD;

/** A serializable object containing some of the money things. */
public class MoneyStuffV1 {
    private final Currency mainCurrency;
    private final Money<USD> balanceUs;
    private final Money<CAD> balanceCa;
    private final Money<USD> debtUs;

    /** Constructor. */
    @JsonCreator
    public MoneyStuffV1(
            @JsonProperty("mainCurrency") final Currency mainCurrency,
            @JsonProperty("balanceUs") final Money<USD> balanceUs,
            @JsonProperty("balanceCa") final Money<CAD> balanceCa,
            @JsonProperty("debtUs") final Money<USD> debtUs
    ) {
        this.mainCurrency = mainCurrency;
        this.balanceUs = balanceUs;
        this.balanceCa = balanceCa;
        this.debtUs = debtUs;
    }

    public Currency getMainCurrency() {
        return mainCurrency;
    }

    public Money<USD> getBalanceUs() {
        return balanceUs;
    }

    public Money<CAD> getBalanceCa() {
        return balanceCa;
    }

    public Money<USD> getDebtUs() {
        return debtUs;
    }
}
