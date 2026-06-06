package com.bodymatch.membership.plan;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class MembershipPlanService {
    private final MembershipPlanRepository planRepository;

    public MembershipPlanService(MembershipPlanRepository planRepository) {
        this.planRepository = planRepository;
    }

    public List<MembershipPlan> findAll(boolean onlyActive) {
        return onlyActive ? planRepository.findAllByActiveTrue() : planRepository.findAll();
    }

    public Optional<MembershipPlan> findByCode(String code) {
        return planRepository.findByCode(code);
    }

    @Transactional
    public Optional<MembershipPlan> create(CreateMembershipPlanRequest request) {
        if (planRepository.existsByCode(request.code())) {
            throw new IllegalArgumentException("Membership plan code already exists: " + request.code());
        }
        var plan = new MembershipPlan(
                request.code(),
                request.name(),
                request.description(),
                new Money(request.priceAmount(), request.currency()),
                BillingPeriod.valueOf(request.billingPeriod()),
                request.stripePriceId());
        planRepository.save(plan);
        return Optional.of(plan);
    }

    @Transactional
    public void seedDefaults() {
        seedIfMissing("BASIC", "Basic Athlete", "Track workouts and basic progression",
                new BigDecimal("9.99"), "USD", BillingPeriod.MONTHLY);
        seedIfMissing("PRO", "Pro Athlete", "Includes AI exercise analysis and nutrition plans",
                new BigDecimal("19.99"), "USD", BillingPeriod.MONTHLY);
        seedIfMissing("ELITE", "Elite Coaching", "Personalized coach matchmaking and unlimited AI feedback",
                new BigDecimal("49.99"), "USD", BillingPeriod.MONTHLY);
    }

    private void seedIfMissing(String code, String name, String description,
                               BigDecimal amount, String currency, BillingPeriod period) {
        if (!planRepository.existsByCode(code)) {
            planRepository.save(new MembershipPlan(code, name, description,
                    new Money(amount, currency), period, null));
        }
    }
}
