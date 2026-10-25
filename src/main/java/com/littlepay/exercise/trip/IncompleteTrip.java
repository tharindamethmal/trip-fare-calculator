package com.littlepay.exercise.trip;

import java.math.BigInteger;

public record IncompleteTrip(BigInteger from) implements MatchedTrip {
}
