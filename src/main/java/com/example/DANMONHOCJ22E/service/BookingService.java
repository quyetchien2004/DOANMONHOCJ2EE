package com.example.DANMONHOCJ22E.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.DANMONHOCJ22E.dto.BookingResponse;
import com.example.DANMONHOCJ22E.dto.CreateBookingRequest;
import com.example.DANMONHOCJ22E.model.Booking;
import com.example.DANMONHOCJ22E.model.BookingStatus;
import com.example.DANMONHOCJ22E.model.RentalMode;
import com.example.DANMONHOCJ22E.model.Room;
import com.example.DANMONHOCJ22E.model.User;
import com.example.DANMONHOCJ22E.model.Voucher;
import com.example.DANMONHOCJ22E.repository.BookingRepository;
import com.example.DANMONHOCJ22E.repository.RoomRepository;
import com.example.DANMONHOCJ22E.repository.UserRepository;

@Service
public class BookingService {

    private final RoomRepository roomRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final PricingService pricingService;
    private final VoucherService voucherService;

    public BookingService(RoomRepository roomRepository,
                          BookingRepository bookingRepository,
                          UserRepository userRepository,
                          PricingService pricingService,
                          VoucherService voucherService) {
        this.roomRepository = roomRepository;
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.pricingService = pricingService;
        this.voucherService = voucherService;
    }

    @Transactional
    public BookingResponse createBooking(CreateBookingRequest request, String username) {
        Long roomId = Objects.requireNonNull(request.getRoomId(), "roomId khong duoc de trong");
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay room"));

        LocalDateTime checkIn = resolveCheckIn(request);
        LocalDateTime checkOut = resolveCheckOut(request);

        validateBookingWindow(request.getRentalMode(), checkIn, checkOut);

        boolean conflicted = !bookingRepository.findConflictingBookings(
                room.getId(),
                checkIn,
                checkOut,
                List.of(BookingStatus.PENDING, BookingStatus.CONFIRMED)
        ).isEmpty();

        if (conflicted) {
            throw new IllegalArgumentException("Phong da duoc dat trong khoang thoi gian nay");
        }

        BigDecimal originalPrice = pricingService.calculateEstimatedPrice(room, request.getRentalMode(), checkIn, checkOut);

        Booking booking = new Booking();
        booking.setRoom(room);
        User user = null;
        if (username != null) {
            user = userRepository.findByUsername(username);
            if (user != null) {
                booking.setUser(user);
            }
        }

        Voucher voucher = voucherService.findAndValidate(request.getVoucherCode(), user);
        BigDecimal discountAmount = voucherService.calculateDiscountAmount(originalPrice, voucher);
        BigDecimal totalPrice = originalPrice.subtract(discountAmount).setScale(2, RoundingMode.HALF_UP);
        if (totalPrice.compareTo(BigDecimal.ZERO) < 0) {
            totalPrice = BigDecimal.ZERO;
        }

        booking.setCustomerFullName(request.getCustomerFullName().trim());
        booking.setRentalMode(request.getRentalMode());
        booking.setCheckInAt(checkIn);
        booking.setCheckOutAt(checkOut);
        booking.setOriginalPrice(originalPrice);
        booking.setDiscountAmount(discountAmount);
        booking.setAppliedVoucherCode(voucher == null ? null : voucher.getCode().toUpperCase());
        booking.setTotalPrice(totalPrice);
        booking.setStatus(BookingStatus.PENDING);

        Booking saved = bookingRepository.save(booking);

        BookingResponse response = new BookingResponse();
        response.setBookingId(saved.getId());
        response.setRoomId(room.getId());
        response.setRoomNumber(room.getRoomNumber());
        response.setBranchName(room.getBranch().getName());
        response.setCustomerFullName(saved.getCustomerFullName());
        response.setRentalMode(saved.getRentalMode());
        response.setCheckInAt(saved.getCheckInAt());
        response.setCheckOutAt(saved.getCheckOutAt());
        response.setOriginalPrice(saved.getOriginalPrice());
        response.setDiscountAmount(saved.getDiscountAmount());
        response.setAppliedVoucherCode(saved.getAppliedVoucherCode());
        response.setTotalPrice(saved.getTotalPrice());
        response.setStatus(saved.getStatus());
        return response;
    }

    public List<BookingResponse> getBookingsOfUser(String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            return List.of();
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

    private LocalDateTime resolveCheckIn(CreateBookingRequest request) {
        if (request.getRentalMode() == RentalMode.HOURLY) {
            return request.getStartDateTime();
        }
        return request.getStartDate() == null ? null : request.getStartDate().atTime(LocalTime.of(14, 0));
    }

    private LocalDateTime resolveCheckOut(CreateBookingRequest request) {
        if (request.getRentalMode() == RentalMode.HOURLY) {
            return request.getEndDateTime();
        }
        return request.getEndDate() == null ? null : request.getEndDate().atTime(LocalTime.NOON);
    }

    private void validateBookingWindow(RentalMode mode, LocalDateTime checkIn, LocalDateTime checkOut) {
        if (mode == null) {
            throw new IllegalArgumentException("rentalMode la bat buoc");
        }
        if (checkIn == null || checkOut == null) {
            throw new IllegalArgumentException("Thoi gian dat phong khong hop le");
        }
        if (!checkOut.isAfter(checkIn)) {
            throw new IllegalArgumentException("checkOut phai sau checkIn");
        }

        if (mode == RentalMode.DAILY) {
            if (checkIn.getHour() != 14 || checkIn.getMinute() != 0) {
                throw new IllegalArgumentException("Thue theo ngay bat buoc check-in luc 14:00");
            }
            if (checkOut.getHour() != 12 || checkOut.getMinute() != 0) {
                throw new IllegalArgumentException("Thue theo ngay bat buoc check-out luc 12:00");
            }
            if (ChronoUnit.DAYS.between(checkIn.toLocalDate(), checkOut.toLocalDate()) < 1) {
                throw new IllegalArgumentException("Thue theo ngay toi thieu 1 ngay");
            }
        }
    }
}
