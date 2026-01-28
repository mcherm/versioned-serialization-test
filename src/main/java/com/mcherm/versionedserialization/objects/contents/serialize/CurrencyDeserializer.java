package com.mcherm.versionedserialization.objects.contents.serialize;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.mcherm.versionedserialization.objects.contents.Currency;

import java.io.IOException;

/** Deserialization logic for Currency. */
public class CurrencyDeserializer extends JsonDeserializer<Currency> {
    @Override
    public Currency deserialize(JsonParser parser, DeserializationContext context) throws IOException, JacksonException {
        if (parser.getCurrentToken() != JsonToken.VALUE_STRING) {
            final String str = parser.getText();
            try {
                return Currency.fromString(str);
            } catch(IllegalArgumentException err) {
                context.handleWeirdStringValue(Currency.class, str, err.toString());
                return null;
            }
        } else {
            context.handleUnexpectedToken(Currency.class, parser);
            return null;
        }
    }
}
