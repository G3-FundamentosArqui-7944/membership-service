package com.bodymatch.membership.payment;

import com.bodymatch.membership.shared.UserId;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/payments", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Payments", description = "Payment processing and history")
public class PaymentController {
    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/charge")
    public ResponseEntity<PaymentResponse> processPayment(@RequestBody ProcessPaymentRequest request) {
        return paymentService.processPayment(request)
                .map(PaymentResponse::from)
                .map(r -> new ResponseEntity<>(r, HttpStatus.CREATED))
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByUser(@PathVariable Long userId) {
        var resources = paymentService.findPaymentsByUser(new UserId(userId)).stream()
                .map(PaymentResponse::from)
                .toList();
        return ResponseEntity.ok(resources);
    }

    @GetMapping("/user/{userId}/invoices")
    public ResponseEntity<List<InvoiceResponse>> getInvoicesByUser(@PathVariable Long userId) {
        var resources = paymentService.findInvoicesByUser(new UserId(userId)).stream()
                .map(InvoiceResponse::from)
                .toList();
        return ResponseEntity.ok(resources);
    }

    @PostMapping("/webhook/stripe")
    public ResponseEntity<Void> stripeWebhook(@RequestBody String payload,
                                              @RequestHeader("Stripe-Signature") String signature) {
        paymentService.handleStripeWebhook(payload, signature);
        return ResponseEntity.ok().build();
    }
}
