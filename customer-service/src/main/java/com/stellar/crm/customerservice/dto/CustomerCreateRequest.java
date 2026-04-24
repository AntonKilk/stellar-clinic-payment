package com.stellar.crm.customerservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.Optional;

public record CustomerCreateRequest(
        @NotBlank String name,
        @NotBlank @Email String email,
        Optional<String> phoneNumber
) {
}
