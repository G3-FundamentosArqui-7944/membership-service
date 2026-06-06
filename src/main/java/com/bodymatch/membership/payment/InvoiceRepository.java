package com.bodymatch.membership.payment;

import com.bodymatch.membership.shared.UserId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    List<Invoice> findAllByUserIdOrderByIssuedAtDesc(UserId userId);
    Optional<Invoice> findByStripeInvoiceId(String stripeInvoiceId);
}
