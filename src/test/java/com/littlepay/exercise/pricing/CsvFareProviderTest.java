package com.littlepay.exercise.pricing;

import com.littlepay.exercise.pricing.fare.CsvFareProvider;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Currency;

import static com.littlepay.exercise.TestFixtures.stop;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CsvFareProviderTest {

    private static final Currency USD = Currency.getInstance("USD");

    @Test
    void fareBetweenStopsIsReadFromTheFile() {
        assertEquals(usd("5.50"), provider("fares.csv").fareBetween(stop(2), stop(3)));
    }

    @Test
    void fareAppliesInBothDirections() {
        assertEquals(usd("3.25"), provider("fares.csv").fareBetween(stop(2), stop(1)));
    }

    @Test
    void maxFareFromIsTheHighestFareInvolvingTheStop() {
        CsvFareProvider provider = provider("fares.csv");

        assertEquals(usd("7.30"), provider.maxFareFrom(stop(1)));
        assertEquals(usd("5.50"), provider.maxFareFrom(stop(2)));
        assertEquals(usd("7.30"), provider.maxFareFrom(stop(3)));
    }

    @Test
    void currencyIsTheOneGivenToTheConstructor() {
        Currency aud = Currency.getInstance("AUD");
        CsvFareProvider provider = new CsvFareProvider(faresFile("fares.csv"), aud);

        assertEquals(aud, provider.currency());
        assertEquals(new Money(new BigDecimal("3.25"), aud), provider.fareBetween(stop(1), stop(2)));
    }

    @Test
    void unknownStopsAreRejected() {
        CsvFareProvider provider = provider("fares.csv");

        assertThrows(IllegalArgumentException.class, () -> provider.fareBetween(stop(1), stop(9)));
        assertThrows(IllegalArgumentException.class, () -> provider.maxFareFrom(stop(9)));
    }

    @Test
    void faresCanBeReadFromAStream() throws IOException {
        try (InputStream stream = getClass().getResourceAsStream("fares.csv")) {
            assertEquals(usd("7.30"), new CsvFareProvider(stream, USD).fareBetween(stop(1), stop(3)));
        }
    }

    @Test
    void bundledFaresMatchTheExercise() throws IOException {
        Currency aud = Currency.getInstance("AUD");
        try (InputStream stream = CsvFareProvider.class.getResourceAsStream("/fares-aud.csv")) {
            CsvFareProvider provider = new CsvFareProvider(stream, aud);

            assertEquals(new Money(new BigDecimal("3.25"), aud), provider.fareBetween(stop(1), stop(2)));
            assertEquals(new Money(new BigDecimal("5.50"), aud), provider.fareBetween(stop(2), stop(3)));
            assertEquals(new Money(new BigDecimal("7.30"), aud), provider.fareBetween(stop(1), stop(3)));
        }
    }

    @Test
    void duplicateFaresAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> provider("fares-with-duplicate.csv"));
    }

    private CsvFareProvider provider(String resource) {
        return new CsvFareProvider(faresFile(resource), USD);
    }

    /** A fares file kept next to this test in src/test/resources **/
    private Path faresFile(String resource) {
        try {
            return Path.of(getClass().getResource(resource).toURI());
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    private static Money usd(String amount) {
        return new Money(new BigDecimal(amount), USD);
    }
}
