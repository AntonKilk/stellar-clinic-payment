package com.stellar.crm.inventoryservice.repository;

import com.stellar.crm.inventoryservice.model.Group;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public class GroupSpecification {

    private GroupSpecification() {
    }

    public static Specification<Group> byFilter(UUID groupRefId, Boolean hasAvailablePlaces) {
        return Specification
                .where(byGroupRefId(groupRefId))
                .and(hasAvailablePlaces(hasAvailablePlaces));
    }

    private static Specification<Group> byGroupRefId(UUID groupRefId) {
        return (root, query, cb) -> groupRefId == null ? null
                : cb.equal(root.get("groupRefId"), groupRefId);
    }

    private static Specification<Group> hasAvailablePlaces(Boolean available) {
        return (root, query, cb) -> available == null || !available ? null
                : cb.lessThan(root.get("currentCount"), root.get("groupLimit"));
    }
}

