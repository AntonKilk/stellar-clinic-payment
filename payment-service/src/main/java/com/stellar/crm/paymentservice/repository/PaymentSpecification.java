package com.stellar.crm.paymentservice.repository;

import com.stellar.crm.contracts.payment.PaymentStatus;
import com.stellar.crm.paymentservice.model.Payment;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.UUID;

public final class PaymentSpecification {

    private PaymentSpecification() {
    }

    public static Specification<Payment> byFilter(final PaymentFilter filter) {
        return Specification
                .where(hasId(filter.id()))
                .and(hasInquiryRefId(filter.inquiryRefId()))
                .and(hasStatus(filter.status()))
                .and(createdAfter(filter.createdAfter()))
                .and(createdBefore(filter.createdBefore()));
    }

    private static Specification<Payment> hasId(final UUID id) {
        return (root, query, cb) ->
                id == null ? null : cb.equal(root.get("id"), id);
    }

    private static Specification<Payment> hasInquiryRefId(final UUID inquiryRefId) {
        return (root, query, cb) ->
                inquiryRefId == null ? null : cb.equal(root.get("inquiryRefId"), inquiryRefId);
    }

    private static Specification<Payment> hasStatus(final PaymentStatus status) {
        return (root, query, cb) ->
                status == null ? null : cb.equal(root.get("status"), status);
    }

    private static Specification<Payment> createdAfter(final Instant from) {
        return (root, query, cb) ->
                from == null ? null : cb.greaterThanOrEqualTo(root.get("createdAt"), from);
    }

    private static Specification<Payment> createdBefore(final Instant to) {
        return (root, query, cb) ->
                to == null ? null : cb.lessThanOrEqualTo(root.get("createdAt"), to);
    }
}
