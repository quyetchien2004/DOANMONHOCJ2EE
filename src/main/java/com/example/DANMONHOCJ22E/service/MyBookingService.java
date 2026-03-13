package com.example.DANMONHOCJ22E.service;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.DANMONHOCJ22E.dto.BookingResponse;
import com.example.DANMONHOCJ22E.model.Booking;
import com.example.DANMONHOCJ22E.model.User;
import com.example.DANMONHOCJ22E.repository.BookingRepository;
import com.example.DANMONHOCJ22E.repository.UserRepository;

@Service
public class MyBookingService {

    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;

    public MyBookingService(UserRepository userRepository, BookingRepository bookingRepository) {
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
    }

    public List<BookingResponse> getMyBookings(String username) {
        if (username == null || username.isBlank()) {
            return Collections.emptyList();
        }
        User user = userRepository.findByUsername(username);
        if (user == null) {
            return Collections.emptyList();
        }
        return bookingRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private BookingResponse toResponse(Booking booking) {
        BookingResponse response = new BookingResponse();
        response.setBookingId(booking.getId());
        response.setRoomId(booking.getRoom().getId());
        response.setRoomNumber(booking.getRoom().getRoomNumber());
        response.setBranchName(booking.getRoom().getBranch().getName());
        response.setCustomerFullName(booking.getCustomerFullName());
        response.setRentalMode(booking.getRentalMode());
        response.setCheckInAt(booking.getCheckInAt());
        response.setCheckOutAt(booking.getCheckOutAt());
        response.setOriginalPrice(booking.getOriginalPrice());
        response.setDiscountAmount(booking.getDiscountAmount());
        response.setAppliedVoucherCode(booking.getAppliedVoucherCode());
        response.setTotalPrice(booking.getTotalPrice());
        response.setStatus(booking.getStatus());
        return response;
    }
}
