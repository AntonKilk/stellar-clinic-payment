package com.stellar.crm.inquiryservice.repository;

import com.stellar.crm.inquiryservice.model.Inquiry;
import com.stellar.crm.inquiryservice.model.InquiryStatus;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public final class InquirySpecification {

    private InquirySpecification() {
    }

    public static Specification<Inquiry> byFilter(final InquiryFilter filter) {
        return Specification
                .where(hasManagerRefId(filter.managerRefId()))
                .and(hasCustomerRefId(filter.customerRefId()))
                .and(hasProductRefId(filter.productRefId()))
                .and(hasStatus(filter.inquiryStatus()));
    }

    private static Specification<Inquiry> hasCustomerRefId(final UUID customerRefId) {
        return (root, query, cb) ->
                customerRefId == null ? null : cb.equal(root.get("customerRefId"), customerRefId);
    }

    private static Specification<Inquiry> hasManagerRefId(final UUID managerRefId) {
        return (root, query, cb) ->
                managerRefId == null ? null : cb.equal(root.get("managerRefId"), managerRefId);
    }

    private static Specification<Inquiry> hasProductRefId(final UUID productRefId) {
        return (root, query, cb) ->
                productRefId == null ? null : cb.equal(root.get("productRefId"), productRefId);
    }

    private static Specification<Inquiry> hasStatus(final InquiryStatus status) {
        return (root, query, cb) ->
                status == null ? null : cb.equal(root.get("status"), status);
    }
}
