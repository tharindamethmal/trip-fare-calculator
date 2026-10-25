package com.littlepay.exercise.trip;

import java.math.BigInteger;

public record CancelledTrip(BigInteger from) implements MatchedTrip {
}
