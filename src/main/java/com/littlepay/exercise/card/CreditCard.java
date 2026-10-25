package com.littlepay.exercise.card;

public final class CreditCard extends Card {

    private final String primaryAccountNumber;

    public CreditCard(String primaryAccountNumber) {
        this.primaryAccountNumber = primaryAccountNumber;
    }

    public String getPrimaryAccountNumber() {
        return primaryAccountNumber;
    }

    @Override
    public String getUniqueIdentifier() {
        return primaryAccountNumber;
    }
}
