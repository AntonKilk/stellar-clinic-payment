package com.stellar.crm.customerservice.dto;

import java.util.Optional;

public record CustomerCreateRequest(
        String fullName,
        String email,
        Optional<String> phone
) {
}
