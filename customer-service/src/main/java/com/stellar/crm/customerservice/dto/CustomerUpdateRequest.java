package com.stellar.crm.customerservice.dto;

import java.util.Optional;

public record CustomerUpdateRequest(
        Optional<String> fullName,
        Optional<String> email,
        Optional<String> phone
) {
}
