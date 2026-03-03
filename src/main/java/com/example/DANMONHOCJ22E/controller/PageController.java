package com.example.DANMONHOCJ22E.controller;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/about")
    public String about() {
        return "about";
    }

    @GetMapping("/contact")
    public String contact() {
        return "contact";
    }

    @GetMapping("/room")
    public String room() {
        return "room";
    }

    @GetMapping("/shop")
    public String shop() {
        return "shop";
    }

    @GetMapping("/shop-details")
    public String shopDetails() {
        return "shop-details";
    }
}