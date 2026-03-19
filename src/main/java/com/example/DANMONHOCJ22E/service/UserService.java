package com.example.DANMONHOCJ22E.service;

import java.util.List;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.DANMONHOCJ22E.dto.RegisterRequest;
import com.example.DANMONHOCJ22E.model.AccountVerificationStatus;
import com.example.DANMONHOCJ22E.model.User;
import com.example.DANMONHOCJ22E.repository.UserRepository;

@Service
public class UserService {

  private final UserRepository repo;
  private final BCryptPasswordEncoder passwordEncoder;
  private final UserVoucherService userVoucherService;

  public UserService(UserRepository repo,
                     BCryptPasswordEncoder passwordEncoder,
                     UserVoucherService userVoucherService) {
    this.repo = repo;
    this.passwordEncoder = passwordEncoder;
    this.userVoucherService = userVoucherService;
  }

  public List<User> findAll() {
    return repo.findAll();
  }

  public User register(RegisterRequest request) {
    if (repo.existsByUsername(request.getUsername())) {
      throw new IllegalArgumentException("Ten dang nhap da ton tai");
    }

    if (repo.existsByEmail(request.getEmail())) {
      throw new IllegalArgumentException("Email da ton tai");
    }

    User user = new User();
    user.setUsername(request.getUsername().trim());
    user.setPassword(passwordEncoder.encode(request.getPassword()));
    user.setFullName(request.getFullName().trim());
    user.setEmail(request.getEmail().trim().toLowerCase());
    user.setPhone(request.getPhone().trim());
    user.setRole("ROLE_USER");
        user.setTrustScore(0);
        user.setAccountVerificationStatus(AccountVerificationStatus.UNVERIFIED.name());

        User saved = repo.save(user);
        userVoucherService.issueIfMissing(saved,
          UserVoucherService.REASON_NEW_ACCOUNT_10,
          java.math.BigDecimal.TEN,
          45);
        return saved;
  }

  public User save(User u) {
    if (u.getPassword() != null && !u.getPassword().startsWith("$2a$") && !u.getPassword().startsWith("$2b$") && !u.getPassword().startsWith("$2y$")) {
      u.setPassword(passwordEncoder.encode(u.getPassword()));
    }

    if (u.getRole() == null || u.getRole().isBlank()) {
      u.setRole("ROLE_USER");
    }

    return repo.save(u);
  }
}
