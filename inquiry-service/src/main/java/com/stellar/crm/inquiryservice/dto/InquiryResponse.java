package com.stellar.crm.inquiryservice.dto;

import com.stellar.crm.inquiryservice.model.InquirySource;
import com.stellar.crm.inquiryservice.model.InquiryStatus;

import java.time.Instant;
import java.util.UUID;

public record InquiryResponse(
        UUID guid,
        UUID productRefId,
        UUID customerRefId,
        UUID groupRefId,
        UUID managerRefId,
        InquirySource source,
        String comment,
        InquiryStatus status,
        String note,
        Instant createdAt,
        Instant updatedAt
) {
}
