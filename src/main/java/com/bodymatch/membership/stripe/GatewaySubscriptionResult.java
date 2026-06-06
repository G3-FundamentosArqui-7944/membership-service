package com.bodymatch.membership.stripe;

public record GatewaySubscriptionResult(String subscriptionId, String status, String latestInvoiceId) {
}
