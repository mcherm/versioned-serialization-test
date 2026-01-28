package com.mcherm.versionedserialization.objects.contents.serialize;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.mcherm.versionedserialization.objects.contents.Currency;

import java.io.IOException;

/** Serializer for Currency and subclasses. */
public class CurrencySerializer extends JsonSerializer<Currency> {
    @Override
    public void serialize(Currency currency, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeString(currency.currencyCode());
    }
}
