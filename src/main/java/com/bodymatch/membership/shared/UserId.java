package com.bodymatch.membership.shared;

import jakarta.persistence.Embeddable;

@Embeddable
public record UserId(Long userId) {
    public UserId {
        if (userId == null || userId < 0) {
            throw new IllegalArgumentException("The user id must be greater than or equal to zero");
        }
    }

    public UserId() {
        this(0L);
    }
}
