package com.bodymatch.membership.stripe;

public record GatewayChargeRequest(
        String customerId,
        long amountInMinorUnits,
        String currency,
        String paymentMethodId,
        String description) {
}
