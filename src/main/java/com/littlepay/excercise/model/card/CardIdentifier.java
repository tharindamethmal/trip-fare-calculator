package com.littlepay.excercise.model.card;

public sealed interface CardIdentifier permits Pan {
    String value();
}

