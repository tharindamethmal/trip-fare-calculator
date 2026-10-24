package com.littlepay.excercise.model.cards;

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
