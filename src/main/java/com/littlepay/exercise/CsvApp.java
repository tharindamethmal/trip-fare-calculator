package com.littlepay.exercise;

import com.littlepay.exercise.csv.taps.TapCsvReader;
import com.littlepay.exercise.csv.taps.TapRow;
import com.littlepay.exercise.csv.trips.TripCsvWriter;
import com.littlepay.exercise.csv.trips.TripRow;
import com.littlepay.exercise.pricing.ChargeCalculator;
import com.littlepay.exercise.pricing.fare.CsvFareProvider;
import com.littlepay.exercise.pricing.fare.FareProvider;
import com.littlepay.exercise.trip.TripMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Currency;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CsvApp {

    private static final Logger log = LoggerFactory.getLogger(CsvApp.class);

    private static final String FARES_RESOURCE = "/fares-aud.csv";
    private static final Currency FARES_CURRENCY = Currency.getInstance("AUD");

    void main(String[] args) {
        if (args.length < 1 || args.length > 2) {
            System.err.println("Usage: java -jar trip-fare-calculator.jar <taps.csv> [trips.csv]");
            System.exit(2);
        }
        try {
            Path tripsFile = args.length == 2 ? Path.of(args[1]) : uniqueTripsFile();
            run(Path.of(args[0]), tripsFile);
        } catch (IOException | RuntimeException e) {
            log.error("CSV processing failed", e);
            System.exit(1);
        }
    }

    /**
     * trips-yyyyMMdd'T'HHmmss.csv in the current folder, stamped with the current UTC time,
     * with -1, -2, ... added when that name is taken, so earlier results are never overwritten
     **/
    private static Path uniqueTripsFile() {
        String name = "trips-" + DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss").format(LocalDateTime.now(ZoneOffset.UTC));
        Path file = Path.of(name + ".csv");
        for (int suffix = 1; Files.exists(file); suffix++) {
            file = Path.of(name + "-" + suffix + ".csv");
        }
        return file;
    }

    private static void run(Path tapsFile, Path tripsFile) throws IOException {
        log.info("Starting CSV processing");

        List<TapRow> taps;
        try (Reader reader = Files.newBufferedReader(tapsFile)) {
            taps = new TapCsvReader().read(reader);
        }

        Map<String, List<TapRow>> tapsByPan = taps.stream()
                .collect(Collectors.groupingBy(TapRow::pan, LinkedHashMap::new, Collectors.toList()));

        CsvTripService service = new CsvTripService(new TripMatcher(), new ChargeCalculator(fareProvider()));
        List<TripRow> trips = service.chargeTrips(tapsByPan);

        try (Writer writer = Files.newBufferedWriter(tripsFile)) {
            new TripCsvWriter().write(trips, writer);
        }

        log.info("Wrote {} trips from {} taps to {}", trips.size(), taps.size(), tripsFile);
    }

    private static FareProvider fareProvider() throws IOException {
        try (InputStream fares = CsvApp.class.getResourceAsStream(FARES_RESOURCE)) {
            return new CsvFareProvider(fares, FARES_CURRENCY);
        }
    }
}
