package com.littlepay.exercise.pricing;

import com.littlepay.exercise.trip.CancelledTrip;
import com.littlepay.exercise.trip.CompletedTrip;
import com.littlepay.exercise.trip.IncompleteTrip;
import com.littlepay.exercise.trip.MatchedTrip;

import java.math.BigDecimal;

/** Prices a single trip **/
public class ChargeCalculator {

    private final FareProvider fareProvider;

    public ChargeCalculator(FareProvider fareProvider) {
        this.fareProvider = fareProvider;
    }

    public Money calculateCharge(MatchedTrip trip) {
        return switch (trip) {
            case CompletedTrip completed -> fareProvider.fareBetween(completed.from(), completed.to());
            case IncompleteTrip incomplete -> fareProvider.maxFareFrom(incomplete.from());
            case CancelledTrip cancelled -> new Money(BigDecimal.ZERO, fareProvider.currency());
        };
    }
}
