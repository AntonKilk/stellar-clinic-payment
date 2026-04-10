package com.stellar.crm.inquiryservice.dto;

import java.util.UUID;

public record InquiryCreateRequest(
        UUID productRefId,
        UUID customerRefId,
        UUID groupRefId,
        String source,
        String comment
) {
}
