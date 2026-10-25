package com.littlepay.exercise;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.littlepay.exercise.csv.taps.TapRow;
import com.littlepay.exercise.csv.trips.TripRow;
import com.littlepay.exercise.pricing.ChargeCalculator;
import com.littlepay.exercise.pricing.fare.CsvFareProvider;
import com.littlepay.exercise.tap.TapType;
import com.littlepay.exercise.trip.TripMatcher;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

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
import static org.junit.jupiter.api.Assertions.assertTrue;

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
    void tripsAtAStopWithNoFareAreUnmatchedAndTheRestAreStillCharged() {
        CsvTripService service = new CsvTripService(new TripMatcher(), new ChargeCalculator(new CsvFareProvider(
                CsvTripServiceTest.class.getResourceAsStream("/fares-aud.csv"), AUD)));
        Map<String, List<TapRow>> tapsByPan = new LinkedHashMap<>();
        tapsByPan.put(ALICE, List.of(
                tap(0, ON, 1, "Bus37", ALICE), tap(5, OFF, 9, "Bus37", ALICE),     // completed, no fare to stop 9
                tap(10, ON, 9, "Bus37", ALICE), tap(15, OFF, 9, "Bus37", ALICE),   // cancelled at stop 9
                tap(20, ON, 9, "Bus37", ALICE)));                                  // incomplete from stop 9
        tapsByPan.put(BOB, List.of(tap(1, ON, 1, "Bus36", BOB), tap(6, OFF, 2, "Bus36", BOB)));

        List<TripRow> trips = service.chargeTrips(tapsByPan);

        assertEquals(List.of(BOB), trips.stream().map(TripRow::pan).toList());
        assertEquals(new BigDecimal("3.25"), trips.getFirst().chargeAmount());
    }

    @Test
    void tripBetweenAPairOfStopsWithNoFareIsUnmatched() {
        CsvTripService service = new CsvTripService(new TripMatcher(), new ChargeCalculator(new CsvFareProvider(
                CsvTripServiceTest.class.getResourceAsStream("fares-without-1-3.csv"), AUD)));
        Map<String, List<TapRow>> tapsByPan = Map.of(ALICE, List.of(
                tap(0, ON, 1, "Bus37", ALICE), tap(5, OFF, 3, "Bus37", ALICE),     // no 1-3 fare
                tap(10, ON, 1, "Bus37", ALICE), tap(15, OFF, 2, "Bus37", ALICE)));

        List<TripRow> trips = service.chargeTrips(tapsByPan);

        assertEquals(List.of(at(10)), trips.stream().map(TripRow::started).toList());
        assertEquals(new BigDecimal("3.25"), trips.getFirst().chargeAmount());
    }

    @Test
    void tapOffOnAnotherBusStillCompletesTheTripWithTheTapOnsBusAndCompany() {
        TapRow tapOn = new TapRow(at(0), ON, stop(1), "Company1", "Bus37", ALICE);
        TapRow tapOff = new TapRow(at(5), OFF, stop(2), "Company2", "Bus99", ALICE);

        List<TripRow> trips = service.chargeTrips(Map.of(ALICE, List.of(tapOn, tapOff)));

        assertEquals(1, trips.size());
        assertEquals("COMPLETED", trips.getFirst().status());
        assertEquals("Company1", trips.getFirst().companyId());
        assertEquals("Bus37", trips.getFirst().busId());
    }

    @Test
    void unmatchedTripsAreLoggedWithTheirReason() {
        CsvTripService service = new CsvTripService(new TripMatcher(), new ChargeCalculator(new CsvFareProvider(
                CsvTripServiceTest.class.getResourceAsStream("/fares-aud.csv"), AUD)));
        String pan = "5500005555555559";
        Map<String, List<TapRow>> tapsByPan = Map.of(pan, List.of(
                tap(0, OFF, 2, "Bus37", pan),                                      // no tap on
                tap(5, ON, 1, "Bus37", pan), tap(10, OFF, 9, "Bus37", pan)));      // no fare to stop 9

        List<ILoggingEvent> logs = logsWhile(() -> service.chargeTrips(tapsByPan));
        List<String> warnings = messages(logs, Level.WARN);

        assertEquals(2, warnings.size());
        assertTrue(warnings.get(0).contains("tap off with no tap on before it"), warnings.get(0));
        assertTrue(warnings.get(1).contains("no fare for the trip's stops"), warnings.get(1));
        assertTrue(warnings.get(1).contains("ON Stop1") && warnings.get(1).contains("OFF Stop9"), warnings.get(1));
        assertTrue(warnings.stream().allMatch(w -> w.contains("card ****5559") && !w.contains(pan)), warnings.toString());
        assertEquals(List.of("2 unmatched trips not charged: {NO_TAP_ON=1, NO_FARE=1}"), messages(logs, Level.INFO));
    }

    @Test
    void tapOffListedBeforeATapOnAtTheSameTimeStillCompletesTheTrip() {
        TapRow tapOff = new TapRow(at(0), OFF, stop(2), "Company1", "Bus99", ALICE);
        TapRow tapOn = new TapRow(at(0), ON, stop(1), "Company1", "Bus37", ALICE);

        List<TripRow> trips = service.chargeTrips(Map.of(ALICE, List.of(tapOff, tapOn)));

        assertEquals(1, trips.size());
        assertEquals("COMPLETED", trips.getFirst().status());
        assertEquals(stop(1), trips.getFirst().fromStopId());
        assertEquals("Bus37", trips.getFirst().busId());    // taken from the tap on, so the rows lined up
    }

    @Test
    void tapOffWithoutTapOnIsNotCharged() {
        assertEquals(List.of(), service.chargeTrips(Map.of(ALICE, List.of(tap(0, OFF, 2, "Bus37", ALICE)))));
    }

    /** Captures what CsvTripService logs while the action runs **/
    private static List<ILoggingEvent> logsWhile(Runnable action) {
        Logger logger = (Logger) LoggerFactory.getLogger(CsvTripService.class);
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        try {
            action.run();
        } finally {
            logger.detachAppender(appender);
        }
        return appender.list;
    }

    private static List<String> messages(List<ILoggingEvent> logs, Level level) {
        return logs.stream()
                .filter(event -> event.getLevel() == level)
                .map(ILoggingEvent::getFormattedMessage)
                .toList();
    }

    private static TapRow tap(int minute, TapType tapType, int stopId, String busId, String pan) {
        return new TapRow(at(minute), tapType, stop(stopId), "Company1", busId, pan);
    }
}
