package com.stellar.crm.inquiryservice.dto;

import com.stellar.crm.inquiryservice.model.InquirySource;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record InquiryCreateRequest(
        @NotNull UUID productRefId,
        @NotNull UUID customerRefId,
        @NotNull UUID groupRefId,
        @NotNull InquirySource source,
        String comment
) {
}
