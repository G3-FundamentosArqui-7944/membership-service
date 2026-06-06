package com.bodymatch.membership.payment;

import java.math.BigDecimal;
import java.time.Instant;

public record InvoiceResponse(
        Long id,
        Long userId,
        Long subscriptionId,
        BigDecimal amount,
        String currency,
        String status,
        String stripeInvoiceId,
        String hostedInvoiceUrl,
        Instant issuedAt,
        Instant paidAt) {

    public static InvoiceResponse from(Invoice invoice) {
        return new InvoiceResponse(
                invoice.getId(),
                invoice.getUserId().userId(),
                invoice.getSubscriptionId(),
                invoice.getAmount().amount(),
                invoice.getAmount().currency(),
                invoice.getStatus().name(),
                invoice.getStripeInvoiceId(),
                invoice.getHostedInvoiceUrl(),
                invoice.getIssuedAt(),
                invoice.getPaidAt());
    }
}
