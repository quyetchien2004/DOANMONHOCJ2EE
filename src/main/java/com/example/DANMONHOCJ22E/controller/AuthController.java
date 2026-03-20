package com.example.DANMONHOCJ22E.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.DANMONHOCJ22E.dto.RegisterRequest;
import com.example.DANMONHOCJ22E.service.UserService;

import jakarta.validation.Valid;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/register")
    public String showRegister(Model model) {
        if (!model.containsAttribute("registerRequest")) {
            model.addAttribute("registerRequest", new RegisterRequest());
        }
        return "register";
    }

    @PostMapping("/register")
    public String processRegister(@Valid @ModelAttribute("registerRequest") RegisterRequest registerRequest,
                                  BindingResult bindingResult,
                                  Model model) {

        if (bindingResult.hasErrors()) {
            return "register";
        }

        if (!registerRequest.getPassword().equals(registerRequest.getConfirmPassword())) {
            model.addAttribute("error", "Mật khẩu xác nhận không khớp");
            return "register";
        }

        try {
            userService.register(registerRequest);
            return "redirect:/login?registered=true";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            return "register";
        }
    }

    @GetMapping("/login")
    public String login(@RequestParam(required = false) String error,
                        @RequestParam(required = false) String logout,
                        @RequestParam(required = false) String registered,
                        @RequestParam(required = false) String passwordReset,
                        Model model) {
        if (error != null) {
            model.addAttribute("error", "Sai tên đăng nhập hoặc mật khẩu");
        }

        if (logout != null) {
            model.addAttribute("message", "Bạn đã đăng xuất thành công");
        }

        if (registered != null) {
            model.addAttribute("message", "Đăng ký thành công, vui lòng đăng nhập");
        }

        if (passwordReset != null) {
            model.addAttribute("message", "Đổi mật khẩu thành công, vui lòng đăng nhập lại");
        }

        return "login";
    }

    @GetMapping("/access-denied")
    public String accessDenied() {
        return "redirect:/";
    }
}

