package com.example.DANMONHOCJ22E.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.example.DANMONHOCJ22E.model.User;
import com.example.DANMONHOCJ22E.model.UserVoucher;
import com.example.DANMONHOCJ22E.repository.UserVoucherRepository;

@Service
public class UserVoucherService {

    public static final String REASON_NEW_ACCOUNT_10 = "NEW_ACCOUNT_10";
    public static final String REASON_SECOND_BOOKING_10 = "SECOND_BOOKING_10";
    public static final String REASON_TRUST_100_25 = "TRUST_100_25";

    private final UserVoucherRepository userVoucherRepository;

    public UserVoucherService(UserVoucherRepository userVoucherRepository) {
        this.userVoucherRepository = userVoucherRepository;
    }

    public List<UserVoucher> getUserVouchers(User user) {
        return userVoucherRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public void issueIfMissing(User user, String reason, BigDecimal percent, int validDays) {
        if (userVoucherRepository.existsByUserAndReason(user, reason)) {
            return;
        }

        UserVoucher uv = new UserVoucher();
        uv.setUser(user);
        uv.setCode(generateCode(reason));
        uv.setDiscountPercent(percent);
        uv.setReason(reason);
        uv.setValidTo(LocalDateTime.now().plusDays(validDays));
        uv.setActive(true);
        userVoucherRepository.save(uv);
    }

    private String generateCode(String reason) {
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        return "GL" + reason.replace("_", "") + suffix;
    }
}
