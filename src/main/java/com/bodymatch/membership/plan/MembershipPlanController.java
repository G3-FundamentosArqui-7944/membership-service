package com.bodymatch.membership.plan;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/membership-plans", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Membership Plans", description = "Catalog of subscription plans")
public class MembershipPlanController {
    private final MembershipPlanService planService;

    public MembershipPlanController(MembershipPlanService planService) {
        this.planService = planService;
    }

    @GetMapping
    public ResponseEntity<List<MembershipPlanResponse>> getAll(
            @RequestParam(name = "onlyActive", defaultValue = "true") boolean onlyActive) {
        var resources = planService.findAll(onlyActive).stream().map(MembershipPlanResponse::from).toList();
        return ResponseEntity.ok(resources);
    }

    @GetMapping("/{code}")
    public ResponseEntity<MembershipPlanResponse> getByCode(@PathVariable String code) {
        return planService.findByCode(code)
                .map(MembershipPlanResponse::from)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<MembershipPlanResponse> create(@RequestBody CreateMembershipPlanRequest request) {
        return planService.create(request)
                .map(MembershipPlanResponse::from)
                .map(r -> new ResponseEntity<>(r, HttpStatus.CREATED))
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }
}
