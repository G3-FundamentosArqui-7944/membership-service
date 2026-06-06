package com.bodymatch.membership.subscription;

import java.time.Instant;

public record SubscriptionResponse(
        Long id,
        Long userId,
        String planCode,
        String status,
        Instant startDate,
        Instant currentPeriodEnd,
        Instant cancelledAt,
        String stripeSubscriptionId) {

    public static SubscriptionResponse from(Subscription subscription) {
        return new SubscriptionResponse(
                subscription.getId(),
                subscription.getUserId().userId(),
                subscription.getPlan().getCode(),
                subscription.getStatus().name(),
                subscription.getStartDate(),
                subscription.getCurrentPeriodEnd(),
                subscription.getCancelledAt(),
                subscription.getStripeSubscriptionId());
    }
}
