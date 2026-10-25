package com.littlepay.exercise.pricing;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Currency;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CsvFareProviderTest {

    private static final Currency USD = Currency.getInstance("USD");

    private static final String FARES = """
            From,To,Amount
            1,2,3.25
            2,3,5.50
            1,3,7.30
            """;

    @TempDir
    Path tempDir;

    @Test
    void fareBetweenStopsIsReadFromTheFile() throws IOException {
        assertEquals(usd("5.50"), provider(FARES).fareBetween(stop(2), stop(3)));
    }

    @Test
    void fareAppliesInBothDirections() throws IOException {
        assertEquals(usd("3.25"), provider(FARES).fareBetween(stop(2), stop(1)));
    }

    @Test
    void maxFareFromIsTheHighestFareInvolvingTheStop() throws IOException {
        CsvFareProvider provider = provider(FARES);

        assertEquals(usd("7.30"), provider.maxFareFrom(stop(1)));
        assertEquals(usd("5.50"), provider.maxFareFrom(stop(2)));
        assertEquals(usd("7.30"), provider.maxFareFrom(stop(3)));
    }

    @Test
    void currencyIsTheOneGivenToTheConstructor() throws IOException {
        Currency aud = Currency.getInstance("AUD");
        CsvFareProvider provider = new CsvFareProvider(faresFile(FARES), aud);

        assertEquals(aud, provider.currency());
        assertEquals(new Money(new BigDecimal("3.25"), aud), provider.fareBetween(stop(1), stop(2)));
    }

    @Test
    void unknownStopsAreRejected() throws IOException {
        CsvFareProvider provider = provider(FARES);

        assertThrows(IllegalArgumentException.class, () -> provider.fareBetween(stop(1), stop(9)));
        assertThrows(IllegalArgumentException.class, () -> provider.maxFareFrom(stop(9)));
    }

    @Test
    void faresCanBeReadFromAStream() {
        var stream = new ByteArrayInputStream(FARES.getBytes(StandardCharsets.UTF_8));

        assertEquals(usd("7.30"), new CsvFareProvider(stream, USD).fareBetween(stop(1), stop(3)));
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
        assertThrows(IllegalArgumentException.class, () -> provider(FARES + "2,1,4.00\n"));
    }

    private CsvFareProvider provider(String csv) throws IOException {
        return new CsvFareProvider(faresFile(csv), USD);
    }

    private Path faresFile(String csv) throws IOException {
        return Files.writeString(tempDir.resolve("fares.csv"), csv);
    }

    private static Money usd(String amount) {
        return new Money(new BigDecimal(amount), USD);
    }

    private static BigInteger stop(int stopId) {
        return BigInteger.valueOf(stopId);
    }
}
