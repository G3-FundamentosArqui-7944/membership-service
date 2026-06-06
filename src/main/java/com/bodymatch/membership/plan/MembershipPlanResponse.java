package com.bodymatch.membership.plan;

import java.math.BigDecimal;

public record MembershipPlanResponse(
        Long id,
        String code,
        String name,
        String description,
        BigDecimal priceAmount,
        String currency,
        String billingPeriod,
        boolean active,
        String stripePriceId) {

    public static MembershipPlanResponse from(MembershipPlan plan) {
        return new MembershipPlanResponse(
                plan.getId(),
                plan.getCode(),
                plan.getName(),
                plan.getDescription(),
                plan.getPrice().amount(),
                plan.getPrice().currency(),
                plan.getBillingPeriod().name(),
                plan.isActive(),
                plan.getStripePriceId());
    }
}
