package com.example.DANMONHOCJ22E.service;

import java.math.BigDecimal;

import com.example.DANMONHOCJ22E.model.Booking;
import com.example.DANMONHOCJ22E.model.BookingStatus;
import com.example.DANMONHOCJ22E.model.PaymentOption;

public final class BookingStatusHelper {

    private BookingStatusHelper() {
    }

    public static String resolvePaymentStatus(BookingStatus status) {
        if (status == null) {
            return "PENDING";
        }

        return switch (status) {
            case CONFIRMED -> "SUCCESS";
            case CANCELLED, PAYMENT_FAILED -> "CANCELLED";
            default -> "PENDING";
        };
    }

    public static String resolveWorkflowStatus(Booking booking) {
        if (booking == null || booking.getStatus() == null) {
            return "PENDING";
        }

        boolean isDeposit = booking.getPaymentOption() == PaymentOption.DEPOSIT_30;
        boolean hasPaidDeposit = booking.getPaidAmount() != null && booking.getPaidAmount().compareTo(BigDecimal.ZERO) > 0;
        if (booking.getStatus() == BookingStatus.PENDING && isDeposit && hasPaidDeposit) {
            return "PENDING_APPROVAL";
        }

        return booking.getStatus().name();
    }
}
