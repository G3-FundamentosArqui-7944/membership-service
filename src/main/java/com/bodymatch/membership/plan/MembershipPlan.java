package com.bodymatch.membership.plan;

import com.bodymatch.membership.shared.AuditableAbstractAggregateRoot;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
public class MembershipPlan extends AuditableAbstractAggregateRoot<MembershipPlan> {

    @Getter
    @Column(nullable = false, unique = true, length = 80)
    private String code;

    @Getter
    @Column(nullable = false, length = 120)
    private String name;

    @Getter
    @Column(length = 1000)
    private String description;

    @Embedded
    @Getter
    @AttributeOverride(name = "amount", column = @Column(name = "price_amount", nullable = false))
    @AttributeOverride(name = "currency", column = @Column(name = "price_currency", nullable = false))
    private Money price;

    @Getter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BillingPeriod billingPeriod;

    @Getter
    @Column(nullable = false)
    private boolean active;

    @Getter
    @Column(length = 120)
    private String stripePriceId;

    public MembershipPlan(String code, String name, String description, Money price,
                          BillingPeriod billingPeriod, String stripePriceId) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Plan code must not be blank");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Plan name must not be blank");
        }
        this.code = code;
        this.name = name;
        this.description = description;
        this.price = price;
        this.billingPeriod = billingPeriod;
        this.stripePriceId = stripePriceId;
        this.active = true;
    }

    public void deactivate() { this.active = false; }
    public void activate() { this.active = true; }

    public void updateDetails(String name, String description, Money price) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Plan name must not be blank");
        }
        this.name = name;
        this.description = description;
        this.price = price;
    }

    public void linkStripePrice(String stripePriceId) {
        this.stripePriceId = stripePriceId;
    }
}
