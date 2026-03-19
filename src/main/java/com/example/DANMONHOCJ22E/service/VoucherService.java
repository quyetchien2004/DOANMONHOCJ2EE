package com.example.DANMONHOCJ22E.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

import org.springframework.stereotype.Service;

import com.example.DANMONHOCJ22E.dto.VoucherSelectionResult;
import com.example.DANMONHOCJ22E.model.User;
import com.example.DANMONHOCJ22E.model.UserVoucher;
import com.example.DANMONHOCJ22E.model.Voucher;
import com.example.DANMONHOCJ22E.model.VoucherAudience;
import com.example.DANMONHOCJ22E.repository.BookingRepository;
import com.example.DANMONHOCJ22E.repository.UserVoucherRepository;
import com.example.DANMONHOCJ22E.repository.VoucherRepository;

@Service
public class VoucherService {

    private final VoucherRepository voucherRepository;
    private final UserVoucherRepository userVoucherRepository;
    private final BookingRepository bookingRepository;

    public VoucherService(VoucherRepository voucherRepository,
                          UserVoucherRepository userVoucherRepository,
                          BookingRepository bookingRepository) {
        this.voucherRepository = voucherRepository;
        this.userVoucherRepository = userVoucherRepository;
        this.bookingRepository = bookingRepository;
    }

    public VoucherSelectionResult resolveDiscount(String rawCode, User user, BigDecimal originalPrice) {
        VoucherSelectionResult result = new VoucherSelectionResult();
        if (rawCode == null || rawCode.isBlank()) {
            return result;
        }

        String code = rawCode.trim().toUpperCase(Locale.ROOT);
        LocalDateTime now = LocalDateTime.now();

        if (user != null) {
            UserVoucher personalVoucher = userVoucherRepository.findByCodeIgnoreCase(code).orElse(null);
            if (personalVoucher != null && personalVoucher.getUser().getId().equals(user.getId())) {
                validatePersonalVoucher(personalVoucher, now);
                result.setAppliedCode(personalVoucher.getCode());
                result.setUserVoucherId(personalVoucher.getId());
                result.setDiscountAmount(calculateDiscountAmount(originalPrice, personalVoucher.getDiscountPercent()));
                return result;
            }
        }

        Voucher voucher = voucherRepository.findByCodeIgnoreCase(code)
                .orElseThrow(() -> new IllegalArgumentException("Voucher khong ton tai"));

        validateGlobalVoucher(voucher, user, now);
        result.setAppliedCode(voucher.getCode().toUpperCase(Locale.ROOT));
        result.setDiscountAmount(calculateDiscountAmount(originalPrice, voucher.getDiscountPercent()));
        return result;
    }

    public void markPersonalVoucherUsed(Long userVoucherId) {
        if (userVoucherId == null) {
            return;
        }
        UserVoucher uv = userVoucherRepository.findById(userVoucherId)
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay voucher ca nhan"));
        if (uv.getUsedAt() == null) {
            uv.setUsedAt(LocalDateTime.now());
            uv.setActive(false);
            userVoucherRepository.save(uv);
        }
    }

    private BigDecimal calculateDiscountAmount(BigDecimal originalPrice, BigDecimal discountPercent) {
        return originalPrice
                .multiply(discountPercent)
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
    }

    private void validatePersonalVoucher(UserVoucher voucher, LocalDateTime now) {
        if (!Boolean.TRUE.equals(voucher.getActive())) {
            throw new IllegalArgumentException("Voucher ca nhan khong con hoat dong");
        }
        if (voucher.getUsedAt() != null) {
            throw new IllegalArgumentException("Voucher ca nhan da duoc su dung");
        }
        if (voucher.getValidTo() != null && now.isAfter(voucher.getValidTo())) {
            throw new IllegalArgumentException("Voucher ca nhan da het han");
        }
    }

    private void validateGlobalVoucher(Voucher voucher, User user, LocalDateTime now) {
        if (!Boolean.TRUE.equals(voucher.getActive())) {
            throw new IllegalArgumentException("Voucher khong con hoat dong");
        }

        if (voucher.getValidFrom() != null && now.isBefore(voucher.getValidFrom())) {
            throw new IllegalArgumentException("Voucher chua den thoi gian ap dung");
        }
        if (voucher.getValidTo() != null && now.isAfter(voucher.getValidTo())) {
            throw new IllegalArgumentException("Voucher da het han");
        }

        if (!isAudienceEligible(voucher.getAudience(), user)) {
            throw new IllegalArgumentException("Voucher khong ap dung cho tai khoan hien tai");
        }
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
