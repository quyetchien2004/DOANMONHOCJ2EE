package com.example.DANMONHOCJ22E.service;

import java.time.LocalDateTime;
import java.util.Random;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.DANMONHOCJ22E.model.PasswordResetToken;
import com.example.DANMONHOCJ22E.model.User;
import com.example.DANMONHOCJ22E.repository.PasswordResetTokenRepository;
import com.example.DANMONHOCJ22E.repository.UserRepository;

@Service
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public PasswordResetService(UserRepository userRepository,
                                PasswordResetTokenRepository tokenRepository,
                                PasswordEncoder passwordEncoder,
                                EmailService emailService) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    @Transactional
    public void requestOtp(String rawEmail) {
        if (rawEmail == null || rawEmail.isBlank()) {
            throw new IllegalArgumentException("Email khong duoc de trong");
        }

        User user = userRepository.findByEmail(rawEmail.trim().toLowerCase());
        if (user == null) {
            throw new IllegalArgumentException("Email chua duoc dang ky");
        }

        PasswordResetToken token = new PasswordResetToken();
        token.setUser(user);
        token.setOtpCode(generateOtp());
        token.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        token.setUsed(false);
        tokenRepository.save(token);

        emailService.sendOtp(user.getEmail(), token.getOtpCode());
    }

    @Transactional
    public void resetPassword(String rawEmail, String otpCode, String newPassword) {
        if (newPassword == null || newPassword.length() < 6) {
            throw new IllegalArgumentException("Mat khau moi toi thieu 6 ky tu");
        }

        User user = userRepository.findByEmail(rawEmail.trim().toLowerCase());
        if (user == null) {
            throw new IllegalArgumentException("Email chua duoc dang ky");
        }

        PasswordResetToken token = tokenRepository.findTopByUserAndUsedFalseOrderByCreatedAtDesc(user)
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay OTP con hieu luc"));

        if (!token.getOtpCode().equals(otpCode)) {
            throw new IllegalArgumentException("OTP khong dung");
        }
        if (LocalDateTime.now().isAfter(token.getExpiresAt())) {
            throw new IllegalArgumentException("OTP da het han");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        token.setUsed(true);
        tokenRepository.save(token);
    }

    private String generateOtp() {
        int value = 100000 + new Random().nextInt(900000);
        return String.valueOf(value);
    }
}
