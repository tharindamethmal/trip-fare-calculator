package com.littlepay.exercise.pricing;

import com.littlepay.exercise.trip.CancelledTrip;
import com.littlepay.exercise.trip.CompletedTrip;
import com.littlepay.exercise.trip.IncompleteTrip;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;

import static com.littlepay.exercise.TestFixtures.fixedFares;
import static com.littlepay.exercise.TestFixtures.stop;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ChargeCalculatorTest {

    private static final Currency USD = Currency.getInstance("USD");

    private final ChargeCalculator chargeCalculator = new ChargeCalculator(fixedFares(USD, "3.25", "7.30"));

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
}
