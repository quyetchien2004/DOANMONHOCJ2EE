package com.example.DANMONHOCJ22E.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.DANMONHOCJ22E.model.AccountVerificationStatus;
import com.example.DANMONHOCJ22E.model.Booking;
import com.example.DANMONHOCJ22E.model.BookingInvoice;
import com.example.DANMONHOCJ22E.model.BookingStatus;
import com.example.DANMONHOCJ22E.model.PaymentStatus;
import com.example.DANMONHOCJ22E.model.PaymentTransaction;
import com.example.DANMONHOCJ22E.model.User;
import com.example.DANMONHOCJ22E.repository.BookingInvoiceRepository;
import com.example.DANMONHOCJ22E.repository.BookingRepository;
import com.example.DANMONHOCJ22E.repository.PaymentTransactionRepository;
import com.example.DANMONHOCJ22E.repository.UserRepository;

@Service
public class PaymentService {

    private final BookingRepository bookingRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final BookingInvoiceRepository bookingInvoiceRepository;
    private final UserRepository userRepository;
    private final VoucherService voucherService;
    private final UserVoucherService userVoucherService;
    private final VnPayService vnPayService;

    public PaymentService(BookingRepository bookingRepository,
                          PaymentTransactionRepository paymentTransactionRepository,
                          BookingInvoiceRepository bookingInvoiceRepository,
                          UserRepository userRepository,
                          VoucherService voucherService,
                          UserVoucherService userVoucherService,
                          VnPayService vnPayService) {
        this.bookingRepository = bookingRepository;
        this.paymentTransactionRepository = paymentTransactionRepository;
        this.bookingInvoiceRepository = bookingInvoiceRepository;
        this.userRepository = userRepository;
        this.voucherService = voucherService;
        this.userVoucherService = userVoucherService;
        this.vnPayService = vnPayService;
    }

    @Transactional
    public Booking processVnPayCallback(Map<String, String> params) {
        if (!vnPayService.isValidCallback(params)) {
            throw new IllegalArgumentException("Chu ky callback VNPAY khong hop le");
        }

        String txnRef = required(params, "vnp_TxnRef");
        String[] parts = txnRef.split("-");
        if (parts.length < 1) {
            throw new IllegalArgumentException("TxnRef khong hop le");
        }

        long bookingId = Long.parseLong(parts[0]);
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay booking can thanh toan"));

        PaymentTransaction tx = paymentTransactionRepository.findByTxnRef(txnRef).orElseGet(PaymentTransaction::new);
        tx.setBooking(booking);
        tx.setTxnRef(txnRef);
        tx.setGatewayTransactionNo(params.get("vnp_TransactionNo"));
        tx.setAmount(parseAmount(params.get("vnp_Amount")));

        String responseCode = params.get("vnp_ResponseCode");
        String transStatus = params.get("vnp_TransactionStatus");
        boolean success = "00".equals(responseCode) && "00".equals(transStatus);

        if (success) {
            booking.setPaidAmount(tx.getAmount());
            booking.setStatus(BookingStatus.CONFIRMED);
            tx.setStatus(PaymentStatus.SUCCESS);
            tx.setNote("Thanh toan VNPAY thanh cong");

            voucherService.markPersonalVoucherUsed(booking.getAppliedUserVoucherId());
            createInvoiceIfMissing(booking);
            updateTrustAndRewards(booking);
        } else {
            // Keep compatibility with old SQL Server check constraint on status.
            booking.setStatus(BookingStatus.CANCELLED);
            tx.setStatus(PaymentStatus.FAILED);
            tx.setNote("Thanh toan that bai. ResponseCode=" + responseCode + ", TxnStatus=" + transStatus);
        }

        paymentTransactionRepository.save(tx);
        return bookingRepository.save(booking);
    }

    private String required(Map<String, String> params, String key) {
        String value = params.get(key);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Thieu tham so " + key);
        }
        return value;
    }

    private BigDecimal parseAmount(String rawAmount) {
        if (rawAmount == null || rawAmount.isBlank()) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(rawAmount).divide(new BigDecimal("100"));
    }

    private void createInvoiceIfMissing(Booking booking) {
        if (bookingInvoiceRepository.findByBooking(booking).isPresent()) {
            return;
        }

        BookingInvoice invoice = new BookingInvoice();
        invoice.setBooking(booking);
        String invoiceNo = "INV" + booking.getId() + System.currentTimeMillis();
        invoice.setInvoiceNumber(invoiceNo);
        invoice.setOriginalPrice(booking.getOriginalPrice());
        invoice.setDiscountAmount(booking.getDiscountAmount() == null ? BigDecimal.ZERO : booking.getDiscountAmount());
        invoice.setPaymentAmount(booking.getPaidAmount());
        invoice.setPaymentMethod("VNPAY");
        bookingInvoiceRepository.save(invoice);

        booking.setInvoiceNumber(invoiceNo);
    }

    private void updateTrustAndRewards(Booking booking) {
        User user = booking.getUser();
        if (user == null) {
            return;
        }

        long successfulCount = bookingRepository.countByUserAndStatus(user, BookingStatus.CONFIRMED);
        if (successfulCount == 1 && user.getFirstSuccessfulBookingAt() == null) {
            user.setFirstSuccessfulBookingAt(LocalDateTime.now());
        }

        if (successfulCount == 2) {
            userVoucherService.issueIfMissing(user,
                    UserVoucherService.REASON_SECOND_BOOKING_10,
                    new BigDecimal("10"),
                    60);
        }

        if (AccountVerificationStatus.VERIFIED.name().equals(user.getAccountVerificationStatus()) && successfulCount >= 1) {
            user.setTrustScore(100);
            userVoucherService.issueIfMissing(user,
                    UserVoucherService.REASON_TRUST_100_25,
                    new BigDecimal("25"),
                    90);
        }

        userRepository.save(user);
    }
}
