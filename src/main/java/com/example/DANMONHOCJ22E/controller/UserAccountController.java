package com.example.DANMONHOCJ22E.controller;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.example.DANMONHOCJ22E.model.User;
import com.example.DANMONHOCJ22E.model.UserVoucher;
import com.example.DANMONHOCJ22E.repository.UserRepository;
import com.example.DANMONHOCJ22E.service.AccountVerificationService;
import com.example.DANMONHOCJ22E.service.PasswordResetService;
import com.example.DANMONHOCJ22E.service.UserVoucherService;

@Controller
@RequestMapping
public class UserAccountController {

    private final UserRepository userRepository;
    private final AccountVerificationService accountVerificationService;
    private final UserVoucherService userVoucherService;
    private final PasswordResetService passwordResetService;

    public UserAccountController(UserRepository userRepository,
                                 AccountVerificationService accountVerificationService,
                                 UserVoucherService userVoucherService,
                                 PasswordResetService passwordResetService) {
        this.userRepository = userRepository;
        this.accountVerificationService = accountVerificationService;
        this.userVoucherService = userVoucherService;
        this.passwordResetService = passwordResetService;
    }

    @GetMapping("/my-account")
    public String accountPage(Model model, Principal principal) {
        if (principal != null) {
            User user = userRepository.findByUsername(principal.getName());
            if (user != null) {
                model.addAttribute("accountUser", user);
                model.addAttribute("userVouchers", userVoucherService.getUserVouchers(user));
            }
        }
        return "my-account";
    }

    @GetMapping("/api/account/me")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getMyAccount(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }

        User user = userRepository.findByUsername(principal.getName());
        if (user == null) {
            return ResponseEntity.status(404).build();
        }

        List<UserVoucher> vouchers = userVoucherService.getUserVouchers(user);
        Map<String, Object> body = new HashMap<>();
        body.put("username", user.getUsername());
        body.put("fullName", user.getFullName());
        body.put("email", user.getEmail());
        body.put("phone", user.getPhone());
        body.put("trustScore", user.getTrustScore());
        body.put("verificationStatus", user.getAccountVerificationStatus());
        body.put("idCardVerifiedAt", user.getIdCardVerifiedAt());
        body.put("vouchers", vouchers);
        return ResponseEntity.ok(body);
    }

    @PostMapping("/api/account/verify-idcard")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> verifyIdCard(@RequestParam("cccdImage") MultipartFile cccdImage,
                                                            Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }
        User updated = accountVerificationService.uploadAndVerifyIdCard(principal.getName(), cccdImage);

        Map<String, Object> body = new HashMap<>();
        body.put("message", "Xac minh CCCD thanh cong");
        body.put("trustScore", updated.getTrustScore());
        body.put("verificationStatus", updated.getAccountVerificationStatus());
        return ResponseEntity.ok(body);
    }

    @PostMapping("/api/account/request-password-otp")
    @ResponseBody
    public ResponseEntity<Map<String, String>> requestOtp(@RequestBody Map<String, String> payload,
                                                          Principal principal) {
        passwordResetService.requestOtp(resolveTargetEmail(payload.get("email"), principal));
        return ResponseEntity.ok(Map.of("message", "Da gui OTP den email dang ky"));
    }

    @PostMapping("/api/account/reset-password")
    @ResponseBody
    public ResponseEntity<Map<String, String>> resetPassword(@RequestBody Map<String, String> payload,
                                                             Principal principal) {
        passwordResetService.resetPassword(
                resolveTargetEmail(payload.get("email"), principal),
                payload.get("otpCode"),
                payload.get("newPassword")
        );
        return ResponseEntity.ok(Map.of("message", "Doi mat khau thanh cong"));
    }

    private String resolveTargetEmail(String rawEmail, Principal principal) {
        if (rawEmail != null && !rawEmail.isBlank()) {
            return rawEmail;
        }
        if (principal == null) {
            return rawEmail;
        }

        User user = userRepository.findByUsername(principal.getName());
        return user != null ? user.getEmail() : rawEmail;
    }
}
