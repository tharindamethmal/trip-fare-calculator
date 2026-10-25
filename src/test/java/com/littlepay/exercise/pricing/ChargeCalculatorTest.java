package com.littlepay.exercise.pricing;

import com.littlepay.exercise.trip.CancelledTrip;
import com.littlepay.exercise.trip.CompletedTrip;
import com.littlepay.exercise.trip.IncompleteTrip;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Currency;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ChargeCalculatorTest {

    private static final Currency USD = Currency.getInstance("USD");

    private final FareProvider fareProvider = new FareProvider() {
        @Override
        public Currency currency() {
            return USD;
        }

        @Override
        public Money fareBetween(BigInteger from, BigInteger to) {
            return usd("3.25");
        }

        @Override
        public Money maxFareFrom(BigInteger from) {
            return usd("7.30");
        }
    };

    private final ChargeCalculator chargeCalculator = new ChargeCalculator(fareProvider);

    @Test
    void completedTripIsChargedTheFareBetweenItsStops() {
        assertEquals(usd("3.25"), chargeCalculator.calculateCharge(new CompletedTrip(stop(1), stop(2))));
    }

    @Test
    void incompleteTripIsChargedTheMaxFareFromItsStop() {
        assertEquals(usd("7.30"), chargeCalculator.calculateCharge(new IncompleteTrip(stop(1))));
    }

    @Test
    void cancelledTripIsFree() {
        assertEquals(usd("0"), chargeCalculator.calculateCharge(new CancelledTrip(stop(1))));
    }

    private static Money usd(String amount) {
        return new Money(new BigDecimal(amount), USD);
    }

    private static BigInteger stop(int stopId) {
        return BigInteger.valueOf(stopId);
    }
}
