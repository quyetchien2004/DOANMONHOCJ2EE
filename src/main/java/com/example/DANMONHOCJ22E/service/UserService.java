package com.example.DANMONHOCJ22E.service;

import java.util.List;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.DANMONHOCJ22E.dto.RegisterRequest;
import com.example.DANMONHOCJ22E.model.User;
import com.example.DANMONHOCJ22E.repository.UserRepository;

@Service
public class UserService {

  private final UserRepository repo;
  private final BCryptPasswordEncoder passwordEncoder;

  public UserService(UserRepository repo, BCryptPasswordEncoder passwordEncoder) {
    this.repo = repo;
    this.passwordEncoder = passwordEncoder;
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

    return repo.save(user);
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
