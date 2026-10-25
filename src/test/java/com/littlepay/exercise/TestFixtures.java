package com.littlepay.exercise;

import com.littlepay.exercise.pricing.Money;
import com.littlepay.exercise.pricing.fare.FareProvider;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Currency;

/** Helpers shared by the tests **/
public final class TestFixtures {

    private TestFixtures() {
    }

    /** 22-01-2023 13:mm:00 UTC **/
    public static ZonedDateTime at(int minute) {
        return ZonedDateTime.of(2023, 1, 22, 13, minute, 0, 0, ZoneOffset.UTC);
    }

    public static BigInteger stop(int stopId) {
        return BigInteger.valueOf(stopId);
    }

    /** Charges the same fare for every completed trip and the same max fare for every incomplete one **/
    public static FareProvider fixedFares(Currency currency, String fare, String maxFare) {
        return new FareProvider() {
            @Override
            public Currency currency() {
                return currency;
            }

            @Override
            public Money fareBetween(BigInteger from, BigInteger to) {
                return new Money(new BigDecimal(fare), currency);
            }

            @Override
            public Money maxFareFrom(BigInteger from) {
                return new Money(new BigDecimal(maxFare), currency);
            }
        };
    }
}
