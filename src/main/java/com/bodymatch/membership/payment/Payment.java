package com.bodymatch.membership.payment;

import com.bodymatch.membership.plan.Money;
import com.bodymatch.membership.shared.AuditableAbstractAggregateRoot;
import com.bodymatch.membership.shared.UserId;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@NoArgsConstructor
public class Payment extends AuditableAbstractAggregateRoot<Payment> {

    @Embedded
    @Getter
    @AttributeOverride(name = "userId", column = @Column(name = "user_id", nullable = false))
    private UserId userId;

    @Getter
    @Column(nullable = false)
    private Long subscriptionId;

    @Embedded
    @Getter
    @AttributeOverride(name = "amount", column = @Column(name = "amount", nullable = false))
    @AttributeOverride(name = "currency", column = @Column(name = "currency", nullable = false))
    private Money amount;

    @Getter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;

    @Getter
    @Column(length = 120)
    private String stripePaymentIntentId;

    @Getter
    @Column(length = 200)
    private String description;

    @Getter
    @Column
    private Instant processedAt;

    @Getter
    @Column(length = 500)
    private String failureReason;

    public Payment(UserId userId, Long subscriptionId, Money amount, String description) {
        this.userId = userId;
        this.subscriptionId = subscriptionId;
        this.amount = amount;
        this.description = description;
        this.status = PaymentStatus.PENDING;
    }

    public void markSucceeded(String stripePaymentIntentId) {
        if (status == PaymentStatus.SUCCEEDED) return;
        if (status == PaymentStatus.REFUNDED) {
            throw new IllegalStateException("Cannot mark a refunded payment as succeeded");
        }
        this.status = PaymentStatus.SUCCEEDED;
        this.stripePaymentIntentId = stripePaymentIntentId;
        this.processedAt = Instant.now();
        this.failureReason = null;
    }

    public void markFailed(String reason) {
        this.status = PaymentStatus.FAILED;
        this.failureReason = reason;
        this.processedAt = Instant.now();
    }

    public void markRefunded() {
        if (status != PaymentStatus.SUCCEEDED) {
            throw new IllegalStateException("Only succeeded payments can be refunded");
        }
        this.status = PaymentStatus.REFUNDED;
    }
}
