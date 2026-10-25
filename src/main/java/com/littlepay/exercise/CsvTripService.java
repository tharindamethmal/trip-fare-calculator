package com.littlepay.exercise;

import com.littlepay.exercise.card.Card;
import com.littlepay.exercise.card.CreditCard;
import com.littlepay.exercise.csv.taps.TapRow;
import com.littlepay.exercise.csv.trips.TripRow;
import com.littlepay.exercise.pricing.ChargeCalculator;
import com.littlepay.exercise.tap.Tap;
import com.littlepay.exercise.trip.CancelledTrip;
import com.littlepay.exercise.trip.CompletedTrip;
import com.littlepay.exercise.trip.IncompleteTrip;
import com.littlepay.exercise.trip.MatchedTrip;
import com.littlepay.exercise.trip.Trip;
import com.littlepay.exercise.trip.TripMatcher;
import com.littlepay.exercise.trip.UnmatchedReason;
import com.littlepay.exercise.trip.UnmatchedTrip;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Matches and charges the trips of each card **/
public class CsvTripService {

    private static final Logger log = LoggerFactory.getLogger(CsvTripService.class);

    private final TripMatcher tripMatcher;
    private final ChargeCalculator chargeCalculator;

    public CsvTripService(TripMatcher tripMatcher, ChargeCalculator chargeCalculator) {
        this.tripMatcher = tripMatcher;
        this.chargeCalculator = chargeCalculator;
    }

    /** tapsByPan: each card's taps, keyed by its PAN **/
    public List<TripRow> chargeTrips(Map<String, List<TapRow>> tapsByPan) {
        List<TripRow> trips = new ArrayList<>();
        Map<UnmatchedReason, Integer> unmatchedCounts = new EnumMap<>(UnmatchedReason.class);
        tapsByPan.forEach((pan, taps) -> trips.addAll(chargeTripsOfOneCard(pan, taps, unmatchedCounts)));

        if (!unmatchedCounts.isEmpty()) {
            int total = unmatchedCounts.values().stream().mapToInt(Integer::intValue).sum();
            log.info("{} unmatched trips not charged: {}", total, unmatchedCounts);
        }
        return trips;
    }

    private List<TripRow> chargeTripsOfOneCard(String pan, List<TapRow> taps,
                                               Map<UnmatchedReason, Integer> unmatchedCounts) {
        // same order as TripMatcher: by time, a tap on before a tap off at the same time
        List<TapRow> sorted = taps.stream()
                .sorted(Comparator.comparing(TapRow::timestamp).thenComparing(TapRow::tapType))
                .toList();
        Card card = new CreditCard(pan);
        List<Tap> modelTaps = sorted.stream()
                .map(row -> new Tap(row.timestamp(), row.tapType(), row.stopId(), card))
                .toList();

        List<TripRow> trips = new ArrayList<>();
        int next = 0; // trips come back in tap order, each using the next one or two taps
        for (Trip matched : tripMatcher.matchTrips(modelTaps)) {
            int tapsUsed = tapsUsedBy(matched);
            List<TapRow> rows = sorted.subList(next, next + tapsUsed);
            next += tapsUsed;

            switch (chargeable(matched)) {
                case MatchedTrip trip -> trips.add(toTripRow(trip, rows));
                case UnmatchedTrip unmatched -> {
                    log.warn("Unmatched trip, not charged: {} | card {} | {}",
                            unmatched.reason().description(), masked(pan), describe(rows));
                    unmatchedCounts.merge(unmatched.reason(), 1, Integer::sum);
                }
            }
        }
        return trips;
    }

    /** Only the last four digits, so card numbers never end up in logs **/
    private static String masked(String pan) {
        return pan.length() <= 4 ? "****" : "****" + pan.substring(pan.length() - 4);
    }

    /** e.g. "ON Stop1 at 2023-01-22T13:00Z, OFF Stop9 at 2023-01-22T13:05Z" **/
    private static String describe(List<TapRow> rows) {
        return rows.stream()
                .map(row -> row.tapType() + " Stop" + row.stopId() + " at " + row.timestamp())
                .collect(Collectors.joining(", "));
    }

    /** A matched trip whose stops have no fare cannot be charged, so it is unmatched too **/
    private Trip chargeable(Trip trip) {
        if (trip instanceof MatchedTrip matched && !chargeCalculator.canCharge(matched)) {
            return new UnmatchedTrip(UnmatchedReason.NO_FARE);
        }
        return trip;
    }

    private static int tapsUsedBy(Trip trip) {
        return switch (trip) {
            case CompletedTrip completed -> 2;
            case CancelledTrip cancelled -> 2;
            case IncompleteTrip incomplete -> 1;
            case UnmatchedTrip unmatched -> 1;
        };
    }

    /** rows: the tap on, then the tap off unless the trip is incomplete **/
    private TripRow toTripRow(MatchedTrip trip, List<TapRow> rows) {
        TapRow tapOn = rows.getFirst();
        TapRow tapOff = rows.size() > 1 ? rows.get(1) : null;
        return new TripRow(
                tapOn.timestamp(),
                tapOff == null ? null : tapOff.timestamp(),
                tapOff == null ? null : Duration.between(tapOn.timestamp(), tapOff.timestamp()).toSeconds(),
                tapOn.stopId(),
                tapOff == null ? null : tapOff.stopId(),
                chargeCalculator.calculateCharge(trip).amount(),
                tapOn.companyId(),
                tapOn.busId(),
                tapOn.pan(),
                status(trip));
    }

    private static String status(MatchedTrip trip) {
        return switch (trip) {
            case CompletedTrip completed -> "COMPLETED";
            case IncompleteTrip incomplete -> "INCOMPLETE";
            case CancelledTrip cancelled -> "CANCELLED";
        };
    }
}
