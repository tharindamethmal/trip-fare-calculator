package com.littlepay.exercise.pricing.fare;

import com.littlepay.exercise.pricing.Money;

import java.math.BigInteger;
import java.util.Currency;

/** Supplies fares between stops; implementations decide where the fares come from **/
public interface FareProvider {

    /** The currency all fares from this provider are in **/
    Currency currency();

    /** The fare for travelling between two different stops, in either direction **/
    Money fareBetween(BigInteger from, BigInteger to);

    /** The highest fare reachable from a stop, charged for an incomplete trip **/
    Money maxFareFrom(BigInteger from);
}
