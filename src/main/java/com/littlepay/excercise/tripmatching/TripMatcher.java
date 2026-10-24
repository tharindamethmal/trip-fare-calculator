package com.littlepay.excercise.tripmatching;

import com.littlepay.excercise.model.Tap;
import com.littlepay.excercise.model.TapType;
import com.littlepay.excercise.model.trips.CancelledTrip;
import com.littlepay.excercise.model.trips.CompletedTrip;
import com.littlepay.excercise.model.trips.IncompleteTrip;
import com.littlepay.excercise.model.trips.Trip;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** The state machine: turns one ordered sequence of taps into trips **/
public class TripMatcher {

    private static final Logger log = LoggerFactory.getLogger(TripMatcher.class);

    public List<Trip> matchTrips(List<Tap> taps){
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
                    log.warn("Ignoring tap off at stop {} with no matching tap on", tap.stopId());
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

    private static Trip tripBetween(Tap tapOn, Tap tapOff) {
        if (tapOn.stopId().equals(tapOff.stopId())) {
            return new CancelledTrip(tapOn.stopId());
        }
        return new CompletedTrip(tapOn.stopId(), tapOff.stopId());
    }

    private static List<Tap> sortedByTime(List<Tap> taps) {
        return taps.stream()
                .sorted(Comparator.comparing(Tap::timestamp))
                .toList();
    }
}
