package com.example.DANMONHOCJ22E.config;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.example.DANMONHOCJ22E.model.User;
import com.example.DANMONHOCJ22E.model.Voucher;
import com.example.DANMONHOCJ22E.model.VoucherAudience;
import com.example.DANMONHOCJ22E.repository.UserRepository;
import com.example.DANMONHOCJ22E.repository.VoucherRepository;

@Component
public class IdentityAndVoucherSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final VoucherRepository voucherRepository;
    private final PasswordEncoder passwordEncoder;

    public IdentityAndVoucherSeeder(UserRepository userRepository,
                                    VoucherRepository voucherRepository,
                                    PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.voucherRepository = voucherRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        seedAdmin();
        seedVoucher("WELCOME10", "Giam cho user moi", VoucherAudience.NEW_USER, new BigDecimal("10"));
        seedVoucher("LOYAL10", "Giam cho user quay lai dat phong lan 2", VoucherAudience.LOYAL_USER, new BigDecimal("10"));
        seedVoucher("FREQUENT25", "Giam cho tai khoan verified trust 100", VoucherAudience.FREQUENT_USER, new BigDecimal("25"));
    }

    private void seedAdmin() {
        if (userRepository.existsByUsername("admin")) {
            return;
        }

        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setFullName("System Admin");
        admin.setEmail("admin@ccthotels.vn");
        admin.setPhone("0900000000");
        admin.setRole("ROLE_ADMIN");
        userRepository.save(admin);
    }

    private void seedVoucher(String code, String name, VoucherAudience audience, BigDecimal percent) {
        if (voucherRepository.findByCodeIgnoreCase(code).isPresent()) {
            return;
        }

        Voucher v = new Voucher();
        v.setCode(code);
        v.setName(name);
        v.setAudience(audience);
        v.setDiscountPercent(percent);
        v.setActive(true);
        v.setValidFrom(LocalDateTime.now().minusDays(1));
        v.setValidTo(LocalDateTime.now().plusYears(2));
        voucherRepository.save(v);
    }
}

