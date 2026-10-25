package com.littlepay.exercise.pricing.fare;

import com.littlepay.exercise.pricing.Money;

import java.math.BigInteger;
import java.util.Currency;

/** Supplies fares between stops; implementations decide where the fares come from **/
public interface FareProvider {

    /** The currency all fares from this provider are in **/
    Currency currency();

    /** Whether this provider has any fare to or from the stop **/
    boolean servesStop(BigInteger stopId);

    /** Whether this provider has a fare between the two stops, in either direction **/
    boolean hasFareBetween(BigInteger from, BigInteger to);

    /** The fare for travelling between two different stops, in either direction; NoFareException if there is none **/
    Money fareBetween(BigInteger from, BigInteger to);

    /** The highest fare reachable from a stop, charged for an incomplete trip; NoFareException if there is none **/
    Money maxFareFrom(BigInteger from);
}
