package com.littlepay.exercise.trip;

import java.math.BigInteger;

public record CompletedTrip(BigInteger from, BigInteger to) implements MatchedTrip {
}
