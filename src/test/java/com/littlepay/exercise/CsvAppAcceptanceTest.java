package com.littlepay.exercise;

import com.littlepay.exercise.csv.taps.TapCsvReader;
import com.littlepay.exercise.csv.taps.TapRow;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.io.Reader;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Runs the whole app on every scenario folder under src/test/resources/.../acceptance-test-data,
 * each holding a taps.csv and the expected-trips.csv it should produce; add a folder to add a case
 **/
class CsvAppAcceptanceTest {

    @ParameterizedTest(name = "{0}")
    @MethodSource("scenarios")
    void producesTheExpectedTrips(String scenario, @TempDir Path out) throws IOException {
        Path folder = testData().resolve(scenario);
        Path trips = out.resolve("trips.csv");

        CsvApp.run(folder.resolve("taps.csv"), trips);

        // compared line by line, so the file's line endings don't matter
        assertEquals(Files.readAllLines(folder.resolve("expected-trips.csv")), Files.readAllLines(trips));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("scenarios")
    void usesOnlyTestCardNumbers(String scenario) throws IOException {
        List<TapRow> taps;
        try (Reader reader = Files.newBufferedReader(testData().resolve(scenario).resolve("taps.csv"))) {
            taps = new TapCsvReader().read(reader);
        }

        taps.forEach(tap -> assertTrue(passesLuhn(tap.pan()), "not a valid card number: " + tap.pan()));
    }

    static Stream<String> scenarios() throws IOException {
        try (Stream<Path> folders = Files.list(testData())) {
            return folders.filter(Files::isDirectory).map(folder -> folder.getFileName().toString()).sorted()
                    .toList().stream();
        }
    }

    private static Path testData() {
        try {
            return Path.of(CsvAppAcceptanceTest.class.getResource("acceptance-test-data").toURI());
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    /** The checksum every real card number carries, so a typo in a test PAN is caught **/
    private static boolean passesLuhn(String pan) {
        int sum = 0;
        for (int i = 0; i < pan.length(); i++) {
            int digit = pan.charAt(pan.length() - 1 - i) - '0';
            if (i % 2 == 1) {
                digit *= 2;
                if (digit > 9) {
                    digit -= 9;
                }
            }
            sum += digit;
        }
        return pan.matches("\\d{12,19}") && sum % 10 == 0;
    }
}
