package com.mcherm.versionedserialization.objects.contents;

import com.mcherm.versionedserialization.SerializationUtil;
import com.mcherm.versionedserialization.objects.MoneyStuffV1;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Tests of Currency and its subclasses. */
public class CurrencyTest {

    @Test
    public void testRoundTrip() {
        final Currency obj = new USD();

        final String serialized = SerializationUtil.serialize(obj);
        final String expected = "\"USD\"";
        assertEquals(expected, serialized);

        final Currency deserialized = SerializationUtil.deserialize(serialized, Currency.class);
        assertEquals(obj, deserialized);
    }
}
