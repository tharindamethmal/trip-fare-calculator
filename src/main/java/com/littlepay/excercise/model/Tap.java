package com.littlepay.excercise.model;

import com.littlepay.excercise.model.cards.Card;

import java.math.BigInteger;

public record Tap(TapType tapType, BigInteger stopId, Card card) {
}
