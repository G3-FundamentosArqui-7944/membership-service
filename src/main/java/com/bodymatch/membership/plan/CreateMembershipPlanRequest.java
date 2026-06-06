package com.bodymatch.membership.plan;

import java.math.BigDecimal;

public record CreateMembershipPlanRequest(
        String code,
        String name,
        String description,
        BigDecimal priceAmount,
        String currency,
        String billingPeriod,
        String stripePriceId) {
}
