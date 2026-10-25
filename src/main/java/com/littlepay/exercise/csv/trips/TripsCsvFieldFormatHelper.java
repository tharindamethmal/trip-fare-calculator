package com.littlepay.exercise.csv.trips;

import java.math.BigInteger;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/** How values are written to trips.csv: stops as Stop1, timestamps in UTC as 22-01-2023 13:00:00 **/
final class TripsCsvFieldFormatHelper {

    private static final String STOP_PREFIX = "Stop";
    private static final DateTimeFormatter TIMESTAMP = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    private TripsCsvFieldFormatHelper() {
    }

    static String formatStopId(BigInteger stopId) {
        return STOP_PREFIX + stopId;
    }

    static String formatTimestamp(ZonedDateTime timestamp) {
        return TIMESTAMP.format(timestamp.withZoneSameInstant(ZoneOffset.UTC));
    }
}
