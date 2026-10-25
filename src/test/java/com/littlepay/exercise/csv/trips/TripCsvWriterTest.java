package com.littlepay.exercise.csv.trips;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static com.littlepay.exercise.TestFixtures.at;
import static com.littlepay.exercise.TestFixtures.stop;
import static org.junit.jupiter.api.Assertions.assertEquals;

class TripCsvWriterTest {

    @Test
    void writesOneRowPerTripWithEmptyColumnsForAMissingTapOff() throws IOException {
        List<TripRow> trips = List.of(
                new TripRow(at(0), at(5), 300L, stop(1), stop(2), new BigDecimal("3.25"),
                        "Company1", "Bus37", "5500005555555559", "COMPLETED"),
                new TripRow(at(10), null, null, stop(3), null, new BigDecimal("7.30"),
                        "Company1", "Bus36", "5500005555555559", "INCOMPLETE"));

        StringWriter out = new StringWriter();
        new TripCsvWriter().write(trips, out);

        // compared line by line, so the file's line endings don't matter
        assertEquals(resource("expected-trips.csv").lines().toList(), out.toString().lines().toList());
    }

    /** Reads a file kept next to this test in src/test/resources **/
    private String resource(String name) throws IOException {
        try (InputStream in = getClass().getResourceAsStream(name)) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
