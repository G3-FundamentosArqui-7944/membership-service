package com.bodymatch.membership.subscription;

import java.time.Instant;

public record MembershipValidationResponse(
        Long userId,
        boolean active,
        String planCode,
        Instant currentPeriodEnd) {
}
