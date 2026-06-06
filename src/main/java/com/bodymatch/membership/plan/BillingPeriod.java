package com.bodymatch.membership.plan;

public enum BillingPeriod {
    MONTHLY(30),
    QUARTERLY(90),
    YEARLY(365);

    private final int days;

    BillingPeriod(int days) {
        this.days = days;
    }

    public int days() {
        return days;
    }
}
