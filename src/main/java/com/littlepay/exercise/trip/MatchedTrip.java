package com.littlepay.exercise.trip;

import java.math.BigInteger;

public sealed interface MatchedTrip extends Trip permits CompletedTrip, IncompleteTrip, CancelledTrip {
    BigInteger from();
}
