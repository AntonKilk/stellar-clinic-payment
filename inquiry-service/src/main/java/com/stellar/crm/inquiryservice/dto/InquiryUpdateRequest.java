package com.stellar.crm.inquiryservice.dto;

import com.stellar.crm.inquiryservice.model.InquiryStatus;

import java.util.UUID;

public record InquiryUpdateRequest(
        UUID managerRefId,
        InquiryStatus status
) {
}
