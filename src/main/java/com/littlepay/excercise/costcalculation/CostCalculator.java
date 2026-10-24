package com.littlepay.excercise.costcalculation;

import com.littlepay.excercise.model.Money;
import com.littlepay.excercise.model.trips.CancelledTrip;
import com.littlepay.excercise.model.trips.CompletedTrip;
import com.littlepay.excercise.model.trips.IncompleteTrip;
import com.littlepay.excercise.model.trips.MatchedTrip;

/** Prices a single trip **/
public class CostCalculator {

    public Money calculateCost(MatchedTrip trip) {
        switch (trip) {
            case CompletedTrip completed -> throw new UnsupportedOperationException("Not yet implemented");
            case IncompleteTrip incomplete -> throw new UnsupportedOperationException("Not yet implemented");
            case CancelledTrip cancelled -> throw new UnsupportedOperationException("Not yet implemented");
        }
    }
}
