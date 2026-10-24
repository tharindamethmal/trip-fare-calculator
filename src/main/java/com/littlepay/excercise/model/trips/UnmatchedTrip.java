package com.littlepay.excercise.model.trips;

import java.math.BigInteger;

/** A tap off with no tap on before it **/
public record UnmatchedTrip(BigInteger to) implements Trip {
}
