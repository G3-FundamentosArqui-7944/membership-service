package com.bodymatch.membership.payment;

import com.bodymatch.membership.shared.UserId;
import com.bodymatch.membership.stripe.GatewayChargeRequest;
import com.bodymatch.membership.stripe.GatewayWebhookEvent;
import com.bodymatch.membership.stripe.PaymentGateway;
import com.bodymatch.membership.subscription.Subscription;
import com.bodymatch.membership.subscription.SubscriptionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class PaymentService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;
    private final SubscriptionService subscriptionService;
    private final PaymentGateway paymentGateway;

    public PaymentService(PaymentRepository paymentRepository,
                          InvoiceRepository invoiceRepository,
                          SubscriptionService subscriptionService,
                          PaymentGateway paymentGateway) {
        this.paymentRepository = paymentRepository;
        this.invoiceRepository = invoiceRepository;
        this.subscriptionService = subscriptionService;
        this.paymentGateway = paymentGateway;
    }

    public List<Payment> findPaymentsByUser(UserId userId) {
        return paymentRepository.findAllByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<Invoice> findInvoicesByUser(UserId userId) {
        return invoiceRepository.findAllByUserIdOrderByIssuedAtDesc(userId);
    }

    @Transactional
    public Optional<Payment> processPayment(ProcessPaymentRequest request) {
        var userId = new UserId(request.userId());
        var subscription = subscriptionService.findById(request.subscriptionId())
                .orElseThrow(() -> new IllegalArgumentException("Subscription not found: " + request.subscriptionId()));
        if (!subscription.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Subscription does not belong to user");
        }
        var plan = subscription.getPlan();
        var payment = new Payment(userId, subscription.getId(), plan.getPrice(),
                "Charge for plan " + plan.getCode());
        paymentRepository.save(payment);

        if (subscription.getStripeCustomerId() == null || subscription.getStripeCustomerId().isBlank()) {
            payment.markFailed("Subscription has no Stripe customer linked");
            paymentRepository.save(payment);
            return Optional.of(payment);
        }

        var chargeRequest = new GatewayChargeRequest(
                subscription.getStripeCustomerId(),
                plan.getPrice().toMinorUnits(),
                plan.getPrice().currency(),
                request.paymentMethodId(),
                "BodyMatch AI plan " + plan.getCode());
        var result = paymentGateway.chargePayment(chargeRequest);
        if (result.succeeded()) {
            payment.markSucceeded(result.paymentIntentId());
            subscription.activate(subscription.getStripeSubscriptionId());
            subscriptionService.save(subscription);
        } else {
            payment.markFailed(result.failureMessage());
        }
        paymentRepository.save(payment);
        return Optional.of(payment);
    }

    @Transactional
    public void handleStripeWebhook(String payload, String signature) {
        var event = paymentGateway.parseWebhookEvent(payload, signature);
        LOGGER.info("Received Stripe webhook event: {}", event.type());

        switch (event.type()) {
            case "invoice.payment_succeeded" -> handleInvoicePaid(event);
            case "invoice.payment_failed" -> handleInvoiceFailed(event);
            case "customer.subscription.deleted" -> handleSubscriptionDeleted(event);
            default -> LOGGER.debug("Ignoring webhook event type: {}", event.type());
        }
    }

    private void handleInvoicePaid(GatewayWebhookEvent event) {
        var stripeSubId = event.data().get("subscription");
        if (stripeSubId == null) return;
        subscriptionService.findByStripeSubscriptionId(stripeSubId).ifPresent(s -> {
            s.renew();
            subscriptionService.save(s);
        });
    }

    private void handleInvoiceFailed(GatewayWebhookEvent event) {
        var stripeSubId = event.data().get("subscription");
        if (stripeSubId == null) return;
        subscriptionService.findByStripeSubscriptionId(stripeSubId).ifPresent(s -> {
            s.markPastDue();
            subscriptionService.save(s);
        });
    }

    private void handleSubscriptionDeleted(GatewayWebhookEvent event) {
        var stripeSubId = event.data().get("id");
        if (stripeSubId == null) return;
        subscriptionService.findByStripeSubscriptionId(stripeSubId).ifPresent(s -> {
            s.cancel();
            subscriptionService.save(s);
        });
    }
}
