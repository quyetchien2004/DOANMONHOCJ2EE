package com.example.DANMONHOCJ22E.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.DANMONHOCJ22E.model.Voucher;

public interface VoucherRepository extends JpaRepository<Voucher, Long> {
    Optional<Voucher> findByCodeIgnoreCase(String code);
}
