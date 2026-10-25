package com.littlepay.exercise.csv.taps;

import com.littlepay.exercise.tap.TapType;

import java.math.BigInteger;
import java.time.ZonedDateTime;

/** One line of taps.csv **/
public record TapRow(ZonedDateTime timestamp, TapType tapType, BigInteger stopId,
                     String companyId, String busId, String pan) {
}
