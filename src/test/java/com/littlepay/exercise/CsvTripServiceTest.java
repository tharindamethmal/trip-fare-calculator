package com.littlepay.exercise;

import com.littlepay.exercise.csv.taps.TapRow;
import com.littlepay.exercise.csv.trips.TripRow;
import com.littlepay.exercise.pricing.ChargeCalculator;
import com.littlepay.exercise.tap.TapType;
import com.littlepay.exercise.trip.TripMatcher;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.littlepay.exercise.TestFixtures.at;
import static com.littlepay.exercise.TestFixtures.fixedFares;
import static com.littlepay.exercise.TestFixtures.stop;
import static com.littlepay.exercise.tap.TapType.OFF;
import static com.littlepay.exercise.tap.TapType.ON;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CsvTripServiceTest {

    private static final Currency AUD = Currency.getInstance("AUD");
    private static final String ALICE = "1111";
    private static final String BOB = "2222";

    private final CsvTripService service =
            new CsvTripService(new TripMatcher(), new ChargeCalculator(fixedFares(AUD, "3.25", "7.30")));

    @Test
    void eachCardsTapsBecomeChargedTripRows() {
        Map<String, List<TapRow>> tapsByPan = new LinkedHashMap<>();
        tapsByPan.put(ALICE, List.of(tap(5, OFF, 2, "Bus37", ALICE), tap(0, ON, 1, "Bus37", ALICE)));
        tapsByPan.put(BOB, List.of(tap(1, ON, 3, "Bus36", BOB)));

        assertEquals(List.of(
                new TripRow(at(0), at(5), 300L, stop(1), stop(2), new BigDecimal("3.25"),
                        "Company1", "Bus37", ALICE, "COMPLETED"),
                new TripRow(at(1), null, null, stop(3), null, new BigDecimal("7.30"),
                        "Company1", "Bus36", BOB, "INCOMPLETE")),
                service.chargeTrips(tapsByPan));
    }

    @Test
    void eachTripUsesItsOwnRowsAcrossEveryKindOfTrip() {
        Map<String, List<TapRow>> tapsByPan = Map.of(ALICE, List.of(
                tap(0, OFF, 1, "BusA", ALICE),                                  // unmatched, skipped
                tap(5, ON, 1, "BusB", ALICE), tap(10, OFF, 2, "BusB", ALICE),   // completed
                tap(15, ON, 3, "BusC", ALICE),                                  // incomplete
                tap(20, ON, 2, "BusD", ALICE), tap(25, OFF, 2, "BusD", ALICE))); // cancelled

        List<TripRow> trips = service.chargeTrips(tapsByPan);

        assertEquals(List.of("BusB", "BusC", "BusD"), trips.stream().map(TripRow::busId).toList());
        assertEquals(List.of("COMPLETED", "INCOMPLETE", "CANCELLED"), trips.stream().map(TripRow::status).toList());
        assertEquals(List.of(at(5), at(15), at(20)), trips.stream().map(TripRow::started).toList());
        assertEquals(new BigDecimal("0.00"), trips.get(2).chargeAmount());
    }

    @Test
    void tapOffWithoutTapOnIsNotCharged() {
        assertEquals(List.of(), service.chargeTrips(Map.of(ALICE, List.of(tap(0, OFF, 2, "Bus37", ALICE)))));
    }

    private static TapRow tap(int minute, TapType tapType, int stopId, String busId, String pan) {
        return new TapRow(at(minute), tapType, stop(stopId), "Company1", busId, pan);
    }
}
