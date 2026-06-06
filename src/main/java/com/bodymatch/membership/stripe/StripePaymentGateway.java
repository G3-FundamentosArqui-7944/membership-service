package com.bodymatch.membership.stripe;

import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Subscription;
import com.stripe.net.Webhook;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.SubscriptionCreateParams;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class StripePaymentGateway implements PaymentGateway {

    private static final Logger LOGGER = LoggerFactory.getLogger(StripePaymentGateway.class);

    @Value("${stripe.secret-key:}")
    private String stripeSecretKey;

    @Value("${stripe.webhook.secret:}")
    private String webhookSecret;

    @PostConstruct
    public void init() {
        if (stripeSecretKey != null && !stripeSecretKey.isBlank()) {
            Stripe.apiKey = stripeSecretKey;
            LOGGER.info("Stripe API key configured");
        } else {
            LOGGER.warn("Stripe secret key not configured - payment operations will fail");
        }
    }

    @Override
    public GatewayCustomerResult createCustomer(String email, String fullName) {
        ensureConfigured();
        try {
            var params = CustomerCreateParams.builder()
                    .setEmail(email)
                    .setName(fullName)
                    .build();
            Customer customer = Customer.create(params);
            return new GatewayCustomerResult(customer.getId());
        } catch (StripeException e) {
            throw new IllegalStateException("Failed to create Stripe customer: " + e.getMessage(), e);
        }
    }

    @Override
    public GatewaySubscriptionResult createSubscription(String customerId, String priceId) {
        ensureConfigured();
        try {
            var params = SubscriptionCreateParams.builder()
                    .setCustomer(customerId)
                    .addItem(SubscriptionCreateParams.Item.builder().setPrice(priceId).build())
                    .setPaymentBehavior(SubscriptionCreateParams.PaymentBehavior.DEFAULT_INCOMPLETE)
                    .addAllExpand(java.util.List.of("latest_invoice.payment_intent"))
                    .build();
            Subscription subscription = Subscription.create(params);
            String latestInvoiceId = subscription.getLatestInvoice();
            return new GatewaySubscriptionResult(subscription.getId(), subscription.getStatus(), latestInvoiceId);
        } catch (StripeException e) {
            throw new IllegalStateException("Failed to create Stripe subscription: " + e.getMessage(), e);
        }
    }

    @Override
    public void cancelSubscription(String subscriptionId) {
        ensureConfigured();
        if (subscriptionId == null || subscriptionId.isBlank()) return;
        try {
            Subscription subscription = Subscription.retrieve(subscriptionId);
            subscription.cancel();
        } catch (StripeException e) {
            throw new IllegalStateException("Failed to cancel Stripe subscription: " + e.getMessage(), e);
        }
    }

    @Override
    public GatewayChargeResult chargePayment(GatewayChargeRequest request) {
        ensureConfigured();
        try {
            var builder = PaymentIntentCreateParams.builder()
                    .setAmount(request.amountInMinorUnits())
                    .setCurrency(request.currency().toLowerCase())
                    .setCustomer(request.customerId())
                    .setDescription(request.description())
                    .setConfirm(true)
                    .setOffSession(true);
            if (request.paymentMethodId() != null && !request.paymentMethodId().isBlank()) {
                builder.setPaymentMethod(request.paymentMethodId());
            }
            PaymentIntent intent = PaymentIntent.create(builder.build());
            return new GatewayChargeResult(intent.getId(), intent.getStatus(), null);
        } catch (StripeException e) {
            LOGGER.error("Stripe charge failed: {}", e.getMessage());
            return new GatewayChargeResult(null, "failed", e.getMessage());
        }
    }

    @Override
    public GatewayWebhookEvent parseWebhookEvent(String payload, String signatureHeader) {
        if (webhookSecret == null || webhookSecret.isBlank()) {
            throw new IllegalStateException("Stripe webhook secret not configured");
        }
        try {
            Event event = Webhook.constructEvent(payload, signatureHeader, webhookSecret);
            Map<String, String> data = new HashMap<>();
            Optional.ofNullable(event.getDataObjectDeserializer().getObject().orElse(null))
                    .ifPresent(o -> data.put("object", o.toJson()));
            data.put("id", event.getId());
            return new GatewayWebhookEvent(event.getType(), data);
        } catch (SignatureVerificationException e) {
            throw new IllegalArgumentException("Invalid Stripe webhook signature", e);
        }
    }

    private void ensureConfigured() {
        if (stripeSecretKey == null || stripeSecretKey.isBlank()) {
            throw new IllegalStateException("Stripe API key is not configured");
        }
    }
}
