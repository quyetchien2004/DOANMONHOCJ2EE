package com.example.DANMONHOCJ22E.repository;

import com.example.DANMONHOCJ22E.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
}
