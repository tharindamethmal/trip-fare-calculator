package com.littlepay.exercise.trip;

import com.littlepay.exercise.tap.Tap;
import com.littlepay.exercise.tap.TapType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** The state machine: turns one card's taps, in any order, into trips **/
public class TripMatcher {

    /** Trips come back in tap order, each using the next 2 taps (completed, cancelled) or 1 (incomplete, unmatched) **/
    public List<Trip> matchTrips(List<Tap> taps) {
        List<Trip> trips = new ArrayList<>();
        Tap pendingTapOn = null; // a tap on still waiting for its tap off

        for (Tap tap : sortedByTime(taps)) {
            if (tap.tapType() == TapType.ON) {
                if (pendingTapOn != null) {
                    // the previous tap on never got a tap off
                    trips.add(new IncompleteTrip(pendingTapOn.stopId()));
                }
                pendingTapOn = tap;
            } else {
                if (pendingTapOn == null) {
                    // a tap off with no tap on before it
                    trips.add(new UnmatchedTrip(UnmatchedReason.NO_TAP_ON));
                    continue;
                }
                trips.add(tripBetween(pendingTapOn, tap));
                pendingTapOn = null;
            }
        }

        if (pendingTapOn != null) {
            // the last tap on never got a tap off
            trips.add(new IncompleteTrip(pendingTapOn.stopId()));
        }
        return trips;
    }

    private static MatchedTrip tripBetween(Tap tapOn, Tap tapOff) {
        if (tapOn.stopId().equals(tapOff.stopId())) {
            return new CancelledTrip(tapOn.stopId());
        }
        return new CompletedTrip(tapOn.stopId(), tapOff.stopId());
    }

    private static List<Tap> sortedByTime(List<Tap> taps) {
        return taps.stream()
                .sorted(Comparator.comparing(Tap::timestamp).thenComparing(Tap::tapType))
                .toList();
    }
}
