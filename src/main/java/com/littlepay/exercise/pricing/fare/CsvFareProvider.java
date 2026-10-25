package com.littlepay.exercise.pricing.fare;

import com.littlepay.exercise.pricing.Money;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Currency;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/** Fares loaded from a CSV file with a header row (From,To,Amount); each fare applies in both directions **/
public class CsvFareProvider implements FareProvider {

    private static final CSVFormat FORMAT = CSVFormat.DEFAULT.builder()
            .setHeader()
            .setSkipHeaderRecord(true)
            .setIgnoreSurroundingSpaces(true)
            .setIgnoreEmptyLines(true)
            .build();

    private final Currency currency;
    private final Map<StopPair, Money> fares;

    public CsvFareProvider(Path faresFile, Currency currency) {
        this(load(faresFile, currency), currency);
    }

    /** Reads fares from a stream, e.g. a classpath resource; the caller closes it **/
    public CsvFareProvider(InputStream fares, Currency currency) {
        this(parse(new InputStreamReader(Objects.requireNonNull(fares, "fares"), StandardCharsets.UTF_8), currency),
                currency);
    }

    private CsvFareProvider(Map<StopPair, Money> fares, Currency currency) {
        this.currency = Objects.requireNonNull(currency, "currency");
        this.fares = fares;
    }

    @Override
    public Currency currency() {
        return currency;
    }

    @Override
    public Money fareBetween(BigInteger from, BigInteger to) {
        Money fare = fares.get(StopPair.of(from, to));
        if (fare == null) {
            throw new IllegalArgumentException("No fare between stops " + from + " and " + to);
        }
        return fare;
    }

    @Override
    public Money maxFareFrom(BigInteger from) {
        return fares.entrySet().stream()
                .filter(entry -> entry.getKey().contains(from))
                .map(Map.Entry::getValue)
                .max(Comparator.comparing(Money::amount))
                .orElseThrow(() -> new IllegalArgumentException("No fares from stop " + from));
    }

    private static Map<StopPair, Money> load(Path faresFile, Currency currency) {
        try (Reader reader = Files.newBufferedReader(faresFile)) {
            return parse(reader, currency);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read fares from " + faresFile, e);
        }
    }

    /** Leaves the reader open; whoever opened it closes it **/
    private static Map<StopPair, Money> parse(Reader reader, Currency currency) {
        Map<StopPair, Money> fares = new HashMap<>();
        try {
            for (CSVRecord record : FORMAT.parse(reader)) {
                if (!record.isConsistent()) {
                    throw new IllegalArgumentException("Expected From,To,Amount on line " + record.getRecordNumber());
                }
                StopPair stops = StopPair.of(new BigInteger(record.get("From")), new BigInteger(record.get("To")));
                Money fare = new Money(new BigDecimal(record.get("Amount")), currency);
                if (fares.putIfAbsent(stops, fare) != null) {
                    throw new IllegalArgumentException("Duplicate fare for stops " + stops.low() + " and " + stops.high()
                            + " on line " + record.getRecordNumber());
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot read fares", e);
        }
        return Map.copyOf(fares);
    }

    /** Two stops in a fixed order, so A to B and B to A share one fare **/
    private record StopPair(BigInteger low, BigInteger high) {

        static StopPair of(BigInteger a, BigInteger b) {
            return a.compareTo(b) <= 0 ? new StopPair(a, b) : new StopPair(b, a);
        }

        boolean contains(BigInteger stop) {
            return low.equals(stop) || high.equals(stop);
        }
    }
}
