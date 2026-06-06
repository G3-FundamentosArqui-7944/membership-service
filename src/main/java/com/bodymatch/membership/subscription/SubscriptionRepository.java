package com.bodymatch.membership.subscription;

import com.bodymatch.membership.shared.UserId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    List<Subscription> findAllByUserId(UserId userId);
    Optional<Subscription> findByUserIdAndStatus(UserId userId, SubscriptionStatus status);
    Optional<Subscription> findByStripeSubscriptionId(String stripeSubscriptionId);
}
