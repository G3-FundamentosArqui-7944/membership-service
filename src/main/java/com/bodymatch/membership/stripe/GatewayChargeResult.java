package com.bodymatch.membership.stripe;

public record GatewayChargeResult(
        String paymentIntentId,
        String status,
        String failureMessage) {

    public boolean succeeded() {
        return "succeeded".equalsIgnoreCase(status);
    }
}
