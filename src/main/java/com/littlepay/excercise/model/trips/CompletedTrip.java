package com.littlepay.excercise.model.trips;

import java.math.BigInteger;

public record CompletedTrip(BigInteger from, BigInteger to) implements MatchedTrip {
}
