package com.mcherm.versionedserialization;

import com.mcherm.versionedserialization.objects.MoneyStuffV1;
import com.mcherm.versionedserialization.objects.contents.CAD;
import com.mcherm.versionedserialization.objects.contents.Money;
import com.mcherm.versionedserialization.objects.contents.USD;
import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.assertEquals;

/** Tests of serialization with the MoneyStuffV1 class. */
public class MoneyStuffV1Test {
    @Test
    public void testRoundTrip() {
        final MoneyStuffV1 obj = new MoneyStuffV1(
                new USD(),
                Money.fromString("543.21", new USD()),
                Money.fromString("12.34", new CAD()),
                Money.fromString("666.66", new USD())
        );

        final String serialized = SerializationUtil.serialize(obj);
        final String expected = """
            {\
            "mainCurrency":"USD",\
            "balanceUs":{"amount":543.21,"currency":"USD"},\
            "balanceCa":{"amount":12.34,"currency":"CAD"},\
            "debtUs":{"amount":666.66,"currency":"USD"}\
            }""";
        assertEquals(expected, serialized);

        final MoneyStuffV1 deserialized = SerializationUtil.deserialize(serialized, MoneyStuffV1.class);
        assertEquals(new USD(), deserialized.getMainCurrency());
    }

    @Test
    public void testGenerateSchemaJackson() {
        final String schema = SerializationUtil.generateSchemaJackson(MoneyStuffV1.class);
        final String expected = """
            {"type":"object","id":"urn:jsonschema:com:mcherm:versionedserialization:objects:MoneyStuffV1",\
            "properties":{\
            "mainCurrency":{"type":"any"},\
            "balanceUs":{"type":"object","id":"urn:jsonschema:com:mcherm:versionedserialization:objects:contents:Money<com:mcherm:versionedserialization:objects:contents:USD>","properties":{"amount":{"type":"number"},"currency":{"type":"any"}}},\
            "balanceCa":{"type":"object","id":"urn:jsonschema:com:mcherm:versionedserialization:objects:contents:Money<com:mcherm:versionedserialization:objects:contents:CAD>","properties":{"amount":{"type":"number"},"currency":{"type":"any"}}},\
            "debtUs":{"type":"object","$ref":"urn:jsonschema:com:mcherm:versionedserialization:objects:contents:Money<com:mcherm:versionedserialization:objects:contents:USD>"}\
            }}""";
        assertEquals(expected, schema);
    }

    @Test
    public void testGenerateSchemaVictools() {
        final String schema = SerializationUtil.generateSchemaVictools(MoneyStuffV1.class);
        final String expected = """
            {"$schema":"https://json-schema.org/draft/2020-12/schema",\
            "$defs":{\
            "Money(CAD)":{"type":"object","properties":{"amount":{"type":"number"},"currency":{"type":"string"}}},\
            "Money(USD)":{"type":"object","properties":{"amount":{"type":"number"},"currency":{"type":"string"}}}\
            },\
            "type":"object","properties":{\
            "balanceCa":{"$ref":"#/$defs/Money(CAD)"},\
            "balanceUs":{"$ref":"#/$defs/Money(USD)"},\
            "debtUs":{"$ref":"#/$defs/Money(USD)"},\
            "mainCurrency":{"type":"string"}\
            }}""";
        assertEquals(expected, schema);
    }
}
