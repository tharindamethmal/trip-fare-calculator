package com.littlepay.exercise.trip;

/** Why taps could not be matched into a trip that can be charged **/
public enum UnmatchedReason {

    NO_TAP_ON("tap off with no tap on before it"),
    NO_FARE("no fare for the trip's stops");

    private final String description;

    UnmatchedReason(String description) {
        this.description = description;
    }

    public String description() {
        return description;
    }
}
