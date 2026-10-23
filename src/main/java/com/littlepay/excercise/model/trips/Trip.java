package com.littlepay.excercise.model.trips;

import java.math.BigInteger;

public sealed interface Trip permits CompletedTrip, IncompleteTrip, CancelledTrip {
    BigInteger from();
}
