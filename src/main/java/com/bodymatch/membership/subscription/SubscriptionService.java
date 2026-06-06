package com.bodymatch.membership.subscription;

import com.bodymatch.membership.client.IamGateway;
import com.bodymatch.membership.plan.MembershipPlanRepository;
import com.bodymatch.membership.shared.UserId;
import com.bodymatch.membership.stripe.PaymentGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class SubscriptionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionService.class);

    private final SubscriptionRepository subscriptionRepository;
    private final MembershipPlanRepository planRepository;
    private final PaymentGateway paymentGateway;
    private final IamGateway iamGateway;

    public SubscriptionService(SubscriptionRepository subscriptionRepository,
                               MembershipPlanRepository planRepository,
                               PaymentGateway paymentGateway,
                               IamGateway iamGateway) {
        this.subscriptionRepository = subscriptionRepository;
        this.planRepository = planRepository;
        this.paymentGateway = paymentGateway;
        this.iamGateway = iamGateway;
    }

    public Optional<Subscription> findById(Long id) {
        return subscriptionRepository.findById(id);
    }

    public List<Subscription> findByUser(UserId userId) {
        return subscriptionRepository.findAllByUserId(userId);
    }

    public Optional<Subscription> findActiveByUser(UserId userId) {
        return subscriptionRepository.findByUserIdAndStatus(userId, SubscriptionStatus.ACTIVE);
    }

    public Optional<Subscription> findByStripeSubscriptionId(String stripeSubscriptionId) {
        return subscriptionRepository.findByStripeSubscriptionId(stripeSubscriptionId);
    }

    public Subscription save(Subscription subscription) {
        return subscriptionRepository.save(subscription);
    }

    @Transactional
    public Optional<Subscription> create(CreateSubscriptionRequest request) {
        var userId = new UserId(request.userId());
        if (!iamGateway.existsUserById(request.userId())) {
            throw new IllegalArgumentException("User does not exist: " + request.userId());
        }
        var existingActive = subscriptionRepository.findByUserIdAndStatus(userId, SubscriptionStatus.ACTIVE);
        if (existingActive.isPresent()) {
            throw new IllegalStateException("User already has an active subscription");
        }
        var plan = planRepository.findByCode(request.planCode())
                .orElseThrow(() -> new IllegalArgumentException("Plan not found: " + request.planCode()));
        if (!plan.isActive()) {
            throw new IllegalStateException("Plan is not active: " + request.planCode());
        }

        String customerId = null;
        if (plan.getStripePriceId() != null && !plan.getStripePriceId().isBlank()) {
            try {
                var email = iamGateway.fetchEmailByUserId(request.userId());
                customerId = paymentGateway.createCustomer(email, email).customerId();
            } catch (RuntimeException e) {
                LOGGER.warn("Stripe customer creation skipped: {}", e.getMessage());
            }
        }

        var subscription = new Subscription(userId, plan, customerId);
        subscriptionRepository.save(subscription);
        return Optional.of(subscription);
    }

    @Transactional
    public Optional<Subscription> cancel(Long subscriptionId) {
        var subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new IllegalArgumentException("Subscription not found: " + subscriptionId));
        if (subscription.getStripeSubscriptionId() != null) {
            try {
                paymentGateway.cancelSubscription(subscription.getStripeSubscriptionId());
            } catch (RuntimeException e) {
                LOGGER.warn("Stripe cancel failed (continuing): {}", e.getMessage());
            }
        }
        subscription.cancel();
        subscriptionRepository.save(subscription);
        return Optional.of(subscription);
    }
}
