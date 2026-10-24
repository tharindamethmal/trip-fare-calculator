package com.littlepay.excercise.model.trips;

import java.math.BigInteger;

public record IncompleteTrip(BigInteger from) implements MatchedTrip {
}
