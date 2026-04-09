package com.stellar.crm.customerservice.repository;

import com.stellar.crm.customerservice.model.Customer;
import org.springframework.data.jpa.domain.Specification;

public class CustomerSpecification {

    private CustomerSpecification() {
    }

    public static Specification<Customer> fullNameContains(String fullName) {
        return (root, query, criteriaBuilder) -> {
            if (fullName == null || fullName.isBlank()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("fullName")),
                    "%" + fullName.toLowerCase() + "%"
            );
        };
    }
}
