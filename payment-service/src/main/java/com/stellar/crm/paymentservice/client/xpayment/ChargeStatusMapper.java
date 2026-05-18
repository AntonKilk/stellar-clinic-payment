package com.stellar.crm.paymentservice.client.xpayment;

import com.stellar.crm.contracts.payment.PaymentStatus;
import com.stellar.crm.paymentservice.client.xpayment.dto.ChargeStatus;

public final class ChargeStatusMapper {

    private ChargeStatusMapper() {
    }

    public static PaymentStatus toPaymentStatus(ChargeStatus chargeStatus) {
        return switch (chargeStatus) {
            case PROCESSING -> PaymentStatus.PENDING;
            case SUCCEEDED -> PaymentStatus.APPROVED;
            case CANCELED -> PaymentStatus.DECLINED;
        };
    }

    public static boolean isTerminal(ChargeStatus chargeStatus) {
        return switch (chargeStatus) {
            case PROCESSING -> false;
            case SUCCEEDED, CANCELED -> true;
        };
    }
}
