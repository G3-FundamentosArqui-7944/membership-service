package com.bodymatch.membership.payment;

public record ProcessPaymentRequest(Long userId, Long subscriptionId, String paymentMethodId) {
}
