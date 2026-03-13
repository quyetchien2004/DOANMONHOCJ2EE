package com.example.DANMONHOCJ22E.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import org.springframework.stereotype.Service;

import com.example.DANMONHOCJ22E.model.User;
import com.example.DANMONHOCJ22E.model.Voucher;
import com.example.DANMONHOCJ22E.model.VoucherAudience;
import com.example.DANMONHOCJ22E.repository.BookingRepository;
import com.example.DANMONHOCJ22E.repository.VoucherRepository;

@Service
public class VoucherService {

    private final VoucherRepository voucherRepository;
    private final BookingRepository bookingRepository;

    public VoucherService(VoucherRepository voucherRepository, BookingRepository bookingRepository) {
        this.voucherRepository = voucherRepository;
        this.bookingRepository = bookingRepository;
    }

    public Voucher findAndValidate(String rawCode, User user) {
        if (rawCode == null || rawCode.isBlank()) {
            return null;
        }

        Voucher voucher = voucherRepository.findByCodeIgnoreCase(rawCode.trim())
                .orElseThrow(() -> new IllegalArgumentException("Voucher khong ton tai"));

        if (!Boolean.TRUE.equals(voucher.getActive())) {
            throw new IllegalArgumentException("Voucher khong con hoat dong");
        }

        LocalDateTime now = LocalDateTime.now();
        if (voucher.getValidFrom() != null && now.isBefore(voucher.getValidFrom())) {
            throw new IllegalArgumentException("Voucher chua den thoi gian ap dung");
        }
        if (voucher.getValidTo() != null && now.isAfter(voucher.getValidTo())) {
            throw new IllegalArgumentException("Voucher da het han");
        }

        if (!isAudienceEligible(voucher.getAudience(), user)) {
            throw new IllegalArgumentException("Voucher khong ap dung cho tai khoan hien tai");
        }

        return voucher;
    }

    public BigDecimal calculateDiscountAmount(BigDecimal originalPrice, Voucher voucher) {
        if (voucher == null) {
            return BigDecimal.ZERO;
        }
        return originalPrice
                .multiply(voucher.getDiscountPercent())
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
    }

    private boolean isAudienceEligible(VoucherAudience audience, User user) {
        if (audience == VoucherAudience.ALL) {
            return true;
        }
        if (user == null) {
            return false;
        }

        long bookingCount = bookingRepository.countByUser(user);
        long accountDays = 0;
        if (user.getCreatedAt() != null) {
            accountDays = ChronoUnit.DAYS.between(user.getCreatedAt(), LocalDateTime.now());
        }

        return switch (audience) {
            case NEW_USER -> bookingCount <= 1 || accountDays <= 30;
            case LOYAL_USER -> accountDays >= 180;
            case FREQUENT_USER -> bookingCount >= 5;
            case ALL -> true;
        };
    }
}
