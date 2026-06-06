package com.bodymatch.membership.stripe;

public interface PaymentGateway {
    GatewayCustomerResult createCustomer(String email, String fullName);
    GatewaySubscriptionResult createSubscription(String customerId, String priceId);
    void cancelSubscription(String subscriptionId);
    GatewayChargeResult chargePayment(GatewayChargeRequest request);
    GatewayWebhookEvent parseWebhookEvent(String payload, String signatureHeader);
}
