package com.littlepay.exercise.pricing;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Objects;

/** An amount in a currency, never more precise than the currency allows (e.g. 3.255 AUD is rejected) **/
public record Money(BigDecimal amount, Currency currency) {

    public Money {
        Objects.requireNonNull(amount, "amount");
        Objects.requireNonNull(currency, "currency");
        int decimals = currency.getDefaultFractionDigits();
        try {
            amount = amount.setScale(decimals, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException(
                    amount.toPlainString() + " has more than " + decimals + " decimal places for " + currency, e);
        }
    }
}
