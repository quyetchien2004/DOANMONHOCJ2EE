package com.example.DANMONHOCJ22E.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.example.DANMONHOCJ22E.model.User;
import com.example.DANMONHOCJ22E.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Controller
public class AuthController {

    private final UserRepository repo;
    private final BCryptPasswordEncoder encoder;

    public AuthController(UserRepository repo, BCryptPasswordEncoder encoder) {
        this.repo = repo;
        this.encoder = encoder;
    }

    @GetMapping("/register")
    public String showRegister(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String processRegister(@ModelAttribute User user, Model model) {

        if (repo.existsByUsername(user.getUsername())) {
            model.addAttribute("error", "Username đã tồn tại!");
            return "register";
        }

        if (repo.existsByEmail(user.getEmail())) {
            model.addAttribute("error", "Email đã tồn tại!");
            return "register";
        }

        user.setPassword(encoder.encode(user.getPassword()));
        user.setRole("ROLE_USER");

        repo.save(user);

        return "redirect:/login";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }
}