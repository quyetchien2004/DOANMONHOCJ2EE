package com.example.DANMONHOCJ22E.controller;

import com.example.DANMONHOCJ22E.dto.RegisterRequest;
import com.example.DANMONHOCJ22E.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.BindingResult;

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
            model.addAttribute("error", "Mat khau xac nhan khong khop");
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
                        Model model) {
        if (error != null) {
            model.addAttribute("error", "Sai ten dang nhap hoac mat khau");
        }

        if (logout != null) {
            model.addAttribute("message", "Ban da dang xuat thanh cong");
        }

        if (registered != null) {
            model.addAttribute("message", "Dang ky thanh cong, vui long dang nhap");
        }

        return "login";
    }

    @GetMapping("/access-denied")
    public String accessDenied() {
        return "redirect:/";
    }
}