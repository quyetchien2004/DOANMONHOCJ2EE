package com.example.DANMONHOCJ22E.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.DANMONHOCJ22E.model.Room;

public interface RoomRepository extends JpaRepository<Room, Long> {
    List<Room> findByBranchId(Long branchId);
}
