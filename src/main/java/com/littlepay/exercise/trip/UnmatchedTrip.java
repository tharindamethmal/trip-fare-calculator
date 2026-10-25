package com.littlepay.exercise.trip;

/** Taps that cannot be matched into a trip that can be charged **/
public record UnmatchedTrip(UnmatchedReason reason) implements Trip {
}
