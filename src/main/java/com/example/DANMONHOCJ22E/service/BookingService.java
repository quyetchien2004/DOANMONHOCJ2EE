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
import com.example.DANMONHOCJ22E.dto.VoucherSelectionResult;
import com.example.DANMONHOCJ22E.model.Booking;
import com.example.DANMONHOCJ22E.model.BookingStatus;
import com.example.DANMONHOCJ22E.model.PaymentOption;
import com.example.DANMONHOCJ22E.model.RentalMode;
import com.example.DANMONHOCJ22E.model.Room;
import com.example.DANMONHOCJ22E.model.User;
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
    private final VnPayService vnPayService;

    public BookingService(RoomRepository roomRepository,
                          BookingRepository bookingRepository,
                          UserRepository userRepository,
                          PricingService pricingService,
                          VoucherService voucherService,
                          VnPayService vnPayService) {
        this.roomRepository = roomRepository;
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.pricingService = pricingService;
        this.voucherService = voucherService;
        this.vnPayService = vnPayService;
    }

    @Transactional
    public BookingResponse createBooking(CreateBookingRequest request, String username, String clientIp) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Ban can dang nhap de dat phong va thanh toan");
        }

        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new IllegalArgumentException("Khong tim thay tai khoan dang nhap");
        }

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
            List.of(BookingStatus.PENDING_PAYMENT, BookingStatus.PENDING, BookingStatus.CONFIRMED)
        ).isEmpty();

        if (conflicted) {
            throw new IllegalArgumentException("Phong da duoc dat trong khoang thoi gian nay");
        }

        BigDecimal originalPrice = pricingService.calculateEstimatedPrice(room, request.getRentalMode(), checkIn, checkOut);

        Booking booking = new Booking();
        booking.setRoom(room);
        booking.setUser(user);

        VoucherSelectionResult voucherSelection = voucherService.resolveDiscount(request.getVoucherCode(), user, originalPrice);
        BigDecimal discountAmount = voucherSelection.getDiscountAmount();
        BigDecimal totalPrice = originalPrice.subtract(discountAmount).setScale(2, RoundingMode.HALF_UP);
        if (totalPrice.compareTo(BigDecimal.ZERO) < 0) {
            totalPrice = BigDecimal.ZERO;
        }

        PaymentOption paymentOption = request.getPaymentOption();
        if (paymentOption == null) {
            throw new IllegalArgumentException("Ban can chon hinh thuc thanh toan");
        }
        BigDecimal requiredPaymentAmount = paymentOption == PaymentOption.FULL_100
                ? totalPrice
                : totalPrice.multiply(new BigDecimal("0.30")).setScale(2, RoundingMode.HALF_UP);

        booking.setCustomerFullName(request.getCustomerFullName().trim());
        booking.setRentalMode(request.getRentalMode());
        booking.setCheckInAt(checkIn);
        booking.setCheckOutAt(checkOut);
        booking.setOriginalPrice(originalPrice);
        booking.setDiscountAmount(discountAmount);
        booking.setAppliedVoucherCode(voucherSelection.getAppliedCode());
        booking.setAppliedUserVoucherId(voucherSelection.getUserVoucherId());
        booking.setTotalPrice(totalPrice);
        booking.setPaymentOption(paymentOption);
        booking.setRequiredPaymentAmount(requiredPaymentAmount);
        booking.setPaidAmount(BigDecimal.ZERO);
        // Keep compatibility with old SQL Server check constraint on status.
        booking.setStatus(BookingStatus.PENDING);

        Booking saved = bookingRepository.save(booking);
        String txnRef = saved.getId() + "-" + System.currentTimeMillis();
        String paymentUrl = vnPayService.createPaymentUrl(txnRef, requiredPaymentAmount, clientIp, "Thanh toan booking #" + saved.getId());

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
        response.setPaymentOption(saved.getPaymentOption());
        response.setRequiredPaymentAmount(saved.getRequiredPaymentAmount());
        response.setPaidAmount(saved.getPaidAmount());
        response.setPaymentUrl(paymentUrl);
        response.setInvoiceNumber(saved.getInvoiceNumber());
        response.setStatus(saved.getStatus());
        response.setPaymentStatus(BookingStatusHelper.resolvePaymentStatus(saved.getStatus()));
        response.setWorkflowStatus(BookingStatusHelper.resolveWorkflowStatus(saved));
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
        response.setPaymentOption(booking.getPaymentOption());
        response.setRequiredPaymentAmount(booking.getRequiredPaymentAmount());
        response.setPaidAmount(booking.getPaidAmount());
        response.setInvoiceNumber(booking.getInvoiceNumber());
        response.setStatus(booking.getStatus());
        response.setPaymentStatus(BookingStatusHelper.resolvePaymentStatus(booking.getStatus()));
        response.setWorkflowStatus(BookingStatusHelper.resolveWorkflowStatus(booking));
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
