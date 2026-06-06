package com.bodymatch.membership.plan;

import jakarta.persistence.Embeddable;

import java.math.BigDecimal;

@Embeddable
public record Money(BigDecimal amount, String currency) {

    public Money {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("The money amount must be greater than or equal to zero");
        }
        if (currency == null || currency.isBlank()) {
            throw new IllegalArgumentException("Currency must not be null or empty");
        }
    }

    public Money() {
        this(BigDecimal.ZERO, "USD");
    }

    public long toMinorUnits() {
        return amount.movePointRight(2).longValueExact();
    }
}
