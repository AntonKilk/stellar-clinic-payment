package com.stellar.crm.inquiryservice.repository;

import com.stellar.crm.inquiryservice.model.InquiryStatus;

import java.util.UUID;

public record InquiryFilter(
        InquiryStatus inquiryStatus,
        UUID customerRefId,
        UUID managerRefId,
        UUID productRefId
) {
}


