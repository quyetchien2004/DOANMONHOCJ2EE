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

    @GetMapping("/index-2")
    public String index2() {
        return "index-2";
    }

    @GetMapping("/contact")
    public String contact() {
        return "contact";
    }

    @GetMapping("/faq")
    public String faq() {
        return "faq";
    }

    @GetMapping("/blog")
    public String blog() {
        return "blog";
    }

    @GetMapping("/blog-details")
    public String blogDetails() {
        return "blog-details";
    }

    @GetMapping("/pricing")
    public String pricing() {
        return "pricing";
    }

    @GetMapping("/projects")
    public String projects() {
        return "projects";
    }

    @GetMapping("/services")
    public String services() {
        return "services";
    }

    @GetMapping("/single-rooms")
    public String singleRooms() {
        return "single-rooms";
    }

    @GetMapping("/single-service")
    public String singleService() {
        return "single-service";
    }

    @GetMapping("/team")
    public String team() {
        return "team";
    }

    @GetMapping("/team-single")
    public String teamSingle() {
        return "team-single";
    }

    @GetMapping("/room")
    public String room() {
        return "booking";
    }

    @GetMapping("/booking")
    public String booking() {
        return "booking";
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