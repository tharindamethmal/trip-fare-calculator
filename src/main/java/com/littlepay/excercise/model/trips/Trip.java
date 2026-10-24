package com.littlepay.excercise.model.trips;

public sealed interface Trip permits MatchedTrip, UnmatchedTrip {
}
