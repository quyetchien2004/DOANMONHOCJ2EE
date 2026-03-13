package com.example.DANMONHOCJ22E.controller;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.DANMONHOCJ22E.service.BookingService;

@Controller
public class MyBookingPageController {

    private final BookingService bookingService;

    public MyBookingPageController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping("/my-bookings")
    public String myBookings(Model model, Principal principal) {
        if (principal != null) {
            model.addAttribute("bookings", bookingService.getBookingsOfUser(principal.getName()));
        } else {
            model.addAttribute("bookings", java.util.List.of());
        }
        return "my-bookings";
    }
}
