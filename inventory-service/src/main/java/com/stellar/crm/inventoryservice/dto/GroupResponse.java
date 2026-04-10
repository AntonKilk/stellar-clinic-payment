package com.stellar.crm.inventoryservice.dto;

import java.util.UUID;

public record GroupResponse(
        UUID guid,
        UUID groupRefId,
        int currentCount,
        int groupLimit
) {
}
