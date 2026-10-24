package com.littlepay.excercise.tripmatching;

import com.littlepay.excercise.model.Tap;
import com.littlepay.excercise.model.TapType;
import com.littlepay.excercise.model.cards.CreditCard;
import com.littlepay.excercise.model.trips.CancelledTrip;
import com.littlepay.excercise.model.trips.CompletedTrip;
import com.littlepay.excercise.model.trips.IncompleteTrip;
import com.littlepay.excercise.model.trips.UnmatchedTrip;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;

import static com.littlepay.excercise.model.TapType.OFF;
import static com.littlepay.excercise.model.TapType.ON;
import static org.junit.jupiter.api.Assertions.assertEquals;

class TripMatcherTest {

    private static final CreditCard CARD = new CreditCard("5500005555555559");

    private final TripMatcher tripMatcher = new TripMatcher();

    @Test
    void tapOnAndTapOffAtDifferentStopsIsACompletedTrip() {
        var trips = tripMatcher.matchTrips(List.of(tap(0, ON, 1), tap(5, OFF, 2)));

        assertEquals(List.of(new CompletedTrip(stop(1), stop(2))), trips);
    }

    @Test
    void tapOnAndTapOffAtTheSameStopIsACancelledTrip() {
        var trips = tripMatcher.matchTrips(List.of(tap(0, ON, 1), tap(5, OFF, 1)));

        assertEquals(List.of(new CancelledTrip(stop(1))), trips);
    }

    @Test
    void tapOnWithoutTapOffIsAnIncompleteTrip() {
        var trips = tripMatcher.matchTrips(List.of(tap(0, ON, 2)));

        assertEquals(List.of(new IncompleteTrip(stop(2))), trips);
    }

    @Test
    void tapOnFollowedByAnotherTapOnIsAnIncompleteTrip() {
        var trips = tripMatcher.matchTrips(List.of(tap(0, ON, 1), tap(5, ON, 2), tap(10, OFF, 3)));

        assertEquals(List.of(new IncompleteTrip(stop(1)), new CompletedTrip(stop(2), stop(3))), trips);
    }

    @Test
    void tapOffWithoutTapOnIsAnUnmatchedTrip() {
        var trips = tripMatcher.matchTrips(List.of(tap(0, OFF, 2)));

        assertEquals(List.of(new UnmatchedTrip(stop(2))), trips);
    }

    @Test
    void tapsAreMatchedInTimeOrderRegardlessOfInputOrder() {
        var trips = tripMatcher.matchTrips(List.of(tap(5, OFF, 1), tap(0, ON, 3)));

        assertEquals(List.of(new CompletedTrip(stop(3), stop(1))), trips);
    }

    @Test
    void noTapsGivesNoTrips() {
        assertEquals(List.of(), tripMatcher.matchTrips(List.of()));
    }

    private static Tap tap(int minute, TapType tapType, int stopId) {
        ZonedDateTime timestamp = ZonedDateTime.of(2023, 1, 22, 13, minute, 0, 0, ZoneOffset.UTC);
        return new Tap(timestamp, tapType, stop(stopId), CARD);
    }

    private static BigInteger stop(int stopId) {
        return BigInteger.valueOf(stopId);
    }
}
