package com.littlepay.exercise.tap;

import com.littlepay.exercise.card.Card;

import java.math.BigInteger;
import java.time.ZonedDateTime;

public record Tap(ZonedDateTime timestamp, TapType tapType, BigInteger stopId, Card card) {
}
