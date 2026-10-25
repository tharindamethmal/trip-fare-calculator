package com.littlepay.exercise.csv.trips;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.ZonedDateTime;

/** One line of trips.csv; finished, durationSecs and toStopId are null for an incomplete trip **/
public record TripRow(ZonedDateTime started, ZonedDateTime finished, Long durationSecs,
                      BigInteger fromStopId, BigInteger toStopId, BigDecimal chargeAmount,
                      String companyId, String busId, String pan, String status) {
}
