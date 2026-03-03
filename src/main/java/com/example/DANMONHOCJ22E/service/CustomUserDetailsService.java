package com.example.DANMONHOCJ22E.service;

import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import com.example.DANMONHOCJ22E.repository.UserRepository;
import com.example.DANMONHOCJ22E.model.User;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository repo;

    public CustomUserDetailsService(UserRepository repo) {
        this.repo = repo;
    }

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        User user = repo.findByUsername(username);

        if (user == null)
            throw new UsernameNotFoundException("User not found");

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                Collections.singleton(() -> user.getRole())
        );
    }
}