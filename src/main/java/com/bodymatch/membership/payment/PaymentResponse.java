package com.bodymatch.membership.payment;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentResponse(
        Long id,
        Long userId,
        Long subscriptionId,
        BigDecimal amount,
        String currency,
        String status,
        String stripePaymentIntentId,
        String description,
        Instant processedAt,
        String failureReason) {

    public static PaymentResponse from(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getUserId().userId(),
                payment.getSubscriptionId(),
                payment.getAmount().amount(),
                payment.getAmount().currency(),
                payment.getStatus().name(),
                payment.getStripePaymentIntentId(),
                payment.getDescription(),
                payment.getProcessedAt(),
                payment.getFailureReason());
    }
}
