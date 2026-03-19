package com.example.DANMONHOCJ22E.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.DANMONHOCJ22E.dto.BookingResponse;
import com.example.DANMONHOCJ22E.dto.CreateBookingRequest;
import com.example.DANMONHOCJ22E.service.BookingService;

import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(@Valid @RequestBody CreateBookingRequest request,
                                                         Principal principal,
                                                         HttpServletRequest httpServletRequest) {
        String username = principal != null ? principal.getName() : null;
        String clientIp = httpServletRequest.getRemoteAddr();
        return ResponseEntity.ok(bookingService.createBooking(request, username, clientIp));
    }

    @GetMapping("/my")
    public ResponseEntity<List<BookingResponse>> myBookings(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(bookingService.getBookingsOfUser(principal.getName()));
    }
}
