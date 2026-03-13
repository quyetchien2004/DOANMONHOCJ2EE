package com.example.DANMONHOCJ22E.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.DANMONHOCJ22E.model.HotelBranch;

public interface HotelBranchRepository extends JpaRepository<HotelBranch, Long> {
    List<HotelBranch> findByProvinceContainingIgnoreCase(String province);
}
