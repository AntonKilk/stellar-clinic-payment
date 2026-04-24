package com.stellar.crm.customerservice.dto;

import java.util.Optional;

public record CustomerUpdateRequest(
        Optional<String> name,
        Optional<String> email,
        Optional<String> phoneNumber
) {
}
