package com.stellar.crm.inventoryservice.dto;

import java.util.UUID;

public record GroupCreateRequest(
        UUID groupRefId,
        int groupLimit
) {
}
