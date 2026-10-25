package com.littlepay.exercise.csv.taps;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/** How values are read from taps.csv: stops as Stop1, timestamps in UTC as 22-01-2023 13:00:00 **/
final class TapsCsvFieldFormatHelper {

    private static final String STOP_PREFIX = "Stop";
    private static final DateTimeFormatter TIMESTAMP = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    private TapsCsvFieldFormatHelper() {
    }

    static BigInteger parseStopId(String value) {
        if (!value.startsWith(STOP_PREFIX)) {
            throw new IllegalArgumentException("Expected a stop like Stop1 but got " + value);
        }
        return new BigInteger(value.substring(STOP_PREFIX.length()));
    }

    static ZonedDateTime parseTimestamp(String value) {
        return LocalDateTime.parse(value, TIMESTAMP).atZone(ZoneOffset.UTC);
    }
}
