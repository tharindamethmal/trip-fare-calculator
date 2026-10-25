package com.littlepay.exercise.pricing;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MoneyTest {

    private static final Currency AUD = Currency.getInstance("AUD");

    @Test
    void amountIsPaddedToTheCurrencysDecimalPlaces() {
        assertEquals(new BigDecimal("3.00"), new Money(new BigDecimal("3"), AUD).amount());
    }

    @Test
    void trailingZerosBeyondTheCurrencysDecimalPlacesAreAccepted() {
        assertEquals(new BigDecimal("3.25"), new Money(new BigDecimal("3.250"), AUD).amount());
    }

    @Test
    void amountMorePreciseThanTheCurrencyIsRejectedNotRounded() {
        var e = assertThrows(IllegalArgumentException.class, () -> new Money(new BigDecimal("3.255"), AUD));
        assertEquals("3.255 has more than 2 decimal places for AUD", e.getMessage());
    }
}
