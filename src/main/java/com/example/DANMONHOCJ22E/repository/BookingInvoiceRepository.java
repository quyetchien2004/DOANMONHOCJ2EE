package com.example.DANMONHOCJ22E.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.DANMONHOCJ22E.model.Booking;
import com.example.DANMONHOCJ22E.model.BookingInvoice;

public interface BookingInvoiceRepository extends JpaRepository<BookingInvoice, Long> {
    Optional<BookingInvoice> findByBooking(Booking booking);
}
