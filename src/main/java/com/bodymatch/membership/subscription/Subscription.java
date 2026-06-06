package com.bodymatch.membership.subscription;

import com.bodymatch.membership.plan.MembershipPlan;
import com.bodymatch.membership.shared.AuditableAbstractAggregateRoot;
import com.bodymatch.membership.shared.UserId;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Entity
@NoArgsConstructor
public class Subscription extends AuditableAbstractAggregateRoot<Subscription> {

    @Embedded
    @Getter
    @AttributeOverride(name = "userId", column = @Column(name = "user_id", nullable = false))
    private UserId userId;

    @ManyToOne
    @JoinColumn(name = "plan_id", nullable = false)
    @Getter
    private MembershipPlan plan;

    @Getter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SubscriptionStatus status;

    @Getter
    @Column(nullable = false)
    private Instant startDate;

    @Getter
    @Column(nullable = false)
    private Instant currentPeriodEnd;

    @Getter
    private Instant cancelledAt;

    @Getter
    @Column(length = 120)
    private String stripeSubscriptionId;

    @Getter
    @Column(length = 120)
    private String stripeCustomerId;

    public Subscription(UserId userId, MembershipPlan plan, String stripeCustomerId) {
        if (plan == null) {
            throw new IllegalArgumentException("Plan must not be null");
        }
        if (!plan.isActive()) {
            throw new IllegalStateException("Cannot subscribe to an inactive plan");
        }
        this.userId = userId;
        this.plan = plan;
        this.status = SubscriptionStatus.PENDING;
        this.startDate = Instant.now();
        this.currentPeriodEnd = startDate.plus(plan.getBillingPeriod().days(), ChronoUnit.DAYS);
        this.stripeCustomerId = stripeCustomerId;
    }

    public void activate(String stripeSubscriptionId) {
        if (status == SubscriptionStatus.CANCELED || status == SubscriptionStatus.EXPIRED) {
            throw new IllegalStateException("Cannot activate a canceled or expired subscription");
        }
        this.status = SubscriptionStatus.ACTIVE;
        this.stripeSubscriptionId = stripeSubscriptionId;
    }

    public void renew() {
        if (status == SubscriptionStatus.CANCELED) {
            throw new IllegalStateException("Cannot renew a canceled subscription");
        }
        this.status = SubscriptionStatus.ACTIVE;
        this.currentPeriodEnd = currentPeriodEnd.plus(plan.getBillingPeriod().days(), ChronoUnit.DAYS);
    }

    public void markPastDue() {
        if (status == SubscriptionStatus.CANCELED) return;
        this.status = SubscriptionStatus.PAST_DUE;
    }

    public void cancel() {
        if (status == SubscriptionStatus.CANCELED) return;
        this.status = SubscriptionStatus.CANCELED;
        this.cancelledAt = Instant.now();
    }

    public void expire() {
        this.status = SubscriptionStatus.EXPIRED;
    }

    public boolean isCurrentlyActive() {
        return status == SubscriptionStatus.ACTIVE && Instant.now().isBefore(currentPeriodEnd);
    }
}
