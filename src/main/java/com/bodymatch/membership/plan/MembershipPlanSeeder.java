package com.bodymatch.membership.plan;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class MembershipPlanSeeder {
    private static final Logger LOGGER = LoggerFactory.getLogger(MembershipPlanSeeder.class);
    private final MembershipPlanService planService;

    public MembershipPlanSeeder(MembershipPlanService planService) {
        this.planService = planService;
    }

    @EventListener
    public void on(ApplicationReadyEvent event) {
        LOGGER.info("Seeding membership plans if missing");
        planService.seedDefaults();
    }
}
