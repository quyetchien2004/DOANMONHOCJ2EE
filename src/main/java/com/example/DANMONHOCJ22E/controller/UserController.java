package com.example.DANMONHOCJ22E.controller;

import com.example.DANMONHOCJ22E.model.User;
import com.example.DANMONHOCJ22E.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class UserController {

  @Autowired
  private UserService userService;

  @GetMapping("/users")
  public String list(Model model) {
    model.addAttribute("users", userService.findAll());
    return "users"; // thymeleaf template
  }

  @PostMapping("/users")
  public String save(User user) {
    userService.save(user);
    return "redirect:/users";
  }
}
