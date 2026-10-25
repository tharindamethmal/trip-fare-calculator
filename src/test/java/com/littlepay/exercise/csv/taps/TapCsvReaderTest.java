package com.littlepay.exercise.csv.taps;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;

import static com.littlepay.exercise.tap.TapType.OFF;
import static com.littlepay.exercise.tap.TapType.ON;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TapCsvReaderTest {

    private final TapCsvReader reader = new TapCsvReader();

    @Test
    void readsTapsInTheExerciseFormat() throws IOException {
        List<TapRow> rows = read("taps.csv");

        ZonedDateTime started = ZonedDateTime.of(2023, 1, 22, 13, 0, 0, 0, ZoneOffset.UTC);
        assertEquals(List.of(
                new TapRow(started, ON, BigInteger.ONE, "Company1", "Bus37", "5500005555555559"),
                new TapRow(started.plusMinutes(5), OFF, BigInteger.TWO, "Company1", "Bus37", "5500005555555559")),
                rows);
    }

    @Test
    void invalidStopIsRejectedWithItsRecordNumber() {
        var e = assertThrows(IllegalArgumentException.class, () -> read("taps-invalid-stop.csv"));
        assertEquals("Invalid tap on record 1: Expected a stop like Stop1 but got Station1", e.getMessage());
    }

    /** Reads a CSV file kept next to this test in src/test/resources **/
    private List<TapRow> read(String resource) throws IOException {
        try (Reader csv = new InputStreamReader(getClass().getResourceAsStream(resource), StandardCharsets.UTF_8)) {
            return reader.read(csv);
        }
    }
}
