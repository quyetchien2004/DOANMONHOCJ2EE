package com.example.DANMONHOCJ22E.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.DANMONHOCJ22E.model.User;

public interface UserRepository extends JpaRepository<User, Long> {

    User findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}