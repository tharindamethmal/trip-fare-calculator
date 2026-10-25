package com.littlepay.exercise.pricing;

import com.littlepay.exercise.trip.CancelledTrip;
import com.littlepay.exercise.trip.CompletedTrip;
import com.littlepay.exercise.trip.IncompleteTrip;
import com.littlepay.exercise.trip.MatchedTrip;

/** Prices a single trip **/
public class ChargeCalculator {

    public Money calculateCharge(MatchedTrip trip) {
        switch (trip) {
            case CompletedTrip completed -> throw new UnsupportedOperationException("Not yet implemented");
            case IncompleteTrip incomplete -> throw new UnsupportedOperationException("Not yet implemented");
            case CancelledTrip cancelled -> throw new UnsupportedOperationException("Not yet implemented");
        }
    }
}
