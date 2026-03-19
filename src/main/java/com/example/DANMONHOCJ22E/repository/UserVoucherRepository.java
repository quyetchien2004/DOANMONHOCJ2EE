package com.example.DANMONHOCJ22E.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.DANMONHOCJ22E.model.User;
import com.example.DANMONHOCJ22E.model.UserVoucher;

public interface UserVoucherRepository extends JpaRepository<UserVoucher, Long> {
    Optional<UserVoucher> findByCodeIgnoreCase(String code);

    List<UserVoucher> findByUserOrderByCreatedAtDesc(User user);

    boolean existsByUserAndReason(User user, String reason);
}
