package com.littlepay.excercise.model;

import java.math.BigInteger;

public record Trip(BigInteger from, BigInteger to, TripStatus status) {
}
