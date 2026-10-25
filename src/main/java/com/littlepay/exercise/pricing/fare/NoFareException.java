package com.littlepay.exercise.pricing.fare;

/** Thrown when a fare provider has no fare for the stops of a trip **/
public class NoFareException extends RuntimeException {

    public NoFareException(String message) {
        super(message);
    }
}
