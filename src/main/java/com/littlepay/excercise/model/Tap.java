package com.littlepay.excercise.model;

import com.littlepay.excercise.model.card.CardIdentifier;

import java.math.BigInteger;

public record Tap(TapType tapType, BigInteger stopId, CardIdentifier cardIdentifier) {
}
