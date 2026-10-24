package com.littlepay.excercise.model;

import com.littlepay.excercise.model.cards.Card;

import java.math.BigInteger;
import java.time.ZonedDateTime;

public record Tap(ZonedDateTime timestamp, TapType tapType, BigInteger stopId, Card card) {
}
