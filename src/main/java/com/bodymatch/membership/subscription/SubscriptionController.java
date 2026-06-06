package com.bodymatch.membership.subscription;

import com.bodymatch.membership.shared.UserId;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/subscriptions", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Subscriptions", description = "Subscription lifecycle endpoints")
public class SubscriptionController {
    private final SubscriptionService subscriptionService;

    public SubscriptionController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @PostMapping
    public ResponseEntity<SubscriptionResponse> create(@RequestBody CreateSubscriptionRequest request) {
        return subscriptionService.create(request)
                .map(SubscriptionResponse::from)
                .map(r -> new ResponseEntity<>(r, HttpStatus.CREATED))
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    @DeleteMapping("/{subscriptionId}")
    public ResponseEntity<SubscriptionResponse> cancel(@PathVariable Long subscriptionId) {
        return subscriptionService.cancel(subscriptionId)
                .map(SubscriptionResponse::from)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{subscriptionId}")
    public ResponseEntity<SubscriptionResponse> getById(@PathVariable Long subscriptionId) {
        return subscriptionService.findById(subscriptionId)
                .map(SubscriptionResponse::from)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<SubscriptionResponse>> getByUser(@PathVariable Long userId) {
        var subscriptions = subscriptionService.findByUser(new UserId(userId));
        var resources = subscriptions.stream().map(SubscriptionResponse::from).toList();
        return ResponseEntity.ok(resources);
    }

    @GetMapping("/user/{userId}/membership-status")
    public ResponseEntity<MembershipValidationResponse> validateMembership(@PathVariable Long userId) {
        var active = subscriptionService.findActiveByUser(new UserId(userId));
        if (active.isEmpty() || !active.get().isCurrentlyActive()) {
            return ResponseEntity.ok(new MembershipValidationResponse(userId, false, null, null));
        }
        var s = active.get();
        return ResponseEntity.ok(new MembershipValidationResponse(userId, true, s.getPlan().getCode(), s.getCurrentPeriodEnd()));
    }
}
