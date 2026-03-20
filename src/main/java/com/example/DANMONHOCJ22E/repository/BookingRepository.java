package com.example.DANMONHOCJ22E.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.DANMONHOCJ22E.model.Booking;
import com.example.DANMONHOCJ22E.model.BookingStatus;
import com.example.DANMONHOCJ22E.model.PaymentOption;
import com.example.DANMONHOCJ22E.model.User;

public interface BookingRepository extends JpaRepository<Booking, Long> {

        List<Booking> findByUserOrderByCreatedAtDesc(User user);

        long countByUser(User user);

        long countByUserAndStatus(User user, BookingStatus status);

        @Query("""
                select b from Booking b
                where b.paymentOption = :paymentOption
                  and b.status = :status
                  and b.paidAmount > :minPaidAmount
                order by b.createdAt desc
                """)
        List<Booking> findPendingDepositApprovals(
                @Param("paymentOption") PaymentOption paymentOption,
                @Param("status") BookingStatus status,
                @Param("minPaidAmount") BigDecimal minPaidAmount
        );

    @Query("""
            select b from Booking b
            where b.room.id = :roomId
              and b.status in :activeStatuses
              and b.checkInAt < :checkOut
              and b.checkOutAt > :checkIn
            """)
    List<Booking> findConflictingBookings(
            @Param("roomId") Long roomId,
            @Param("checkIn") LocalDateTime checkIn,
            @Param("checkOut") LocalDateTime checkOut,
            @Param("activeStatuses") List<BookingStatus> activeStatuses
    );
}
