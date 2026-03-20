package com.example.DANMONHOCJ22E.controller;
import java.math.BigDecimal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

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

    @GetMapping("/blog/{id}")
    public String blogDetailById(@PathVariable int id) {
        if (id < 1 || id > 6) {
            return "redirect:/blog";
        }
        return "blog-" + id;
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

    @GetMapping("/payment-result")
    public String paymentResult(@RequestParam(required = false) Long bookingId,
                                @RequestParam(required = false) String status,
                                @RequestParam(required = false) String paymentStatus,
                                @RequestParam(required = false) String workflowStatus,
                                @RequestParam(required = false) BigDecimal totalPrice,
                                @RequestParam(required = false) BigDecimal requiredPaymentAmount,
                                @RequestParam(required = false) BigDecimal paidAmount,
                                @RequestParam(required = false) String invoiceNumber,
                                Model model) {
        model.addAttribute("bookingId", bookingId);
        model.addAttribute("status", status);
        model.addAttribute("paymentStatus", paymentStatus);
        model.addAttribute("workflowStatus", workflowStatus);
        model.addAttribute("totalPrice", totalPrice);
        model.addAttribute("requiredPaymentAmount", requiredPaymentAmount);
        model.addAttribute("paidAmount", paidAmount);
        model.addAttribute("invoiceNumber", invoiceNumber);
        return "payment-result";
    }
}