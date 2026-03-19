package com.example.DANMONHOCJ22E.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.example.DANMONHOCJ22E.model.BookingStatus;
import com.example.DANMONHOCJ22E.model.PaymentOption;
import com.example.DANMONHOCJ22E.model.RentalMode;

public class BookingResponse {

    private Long bookingId;
    private Long roomId;
    private Integer roomNumber;
    private String branchName;
    private String customerFullName;
    private RentalMode rentalMode;
    private LocalDateTime checkInAt;
    private LocalDateTime checkOutAt;
    private BigDecimal originalPrice;
    private BigDecimal discountAmount;
    private String appliedVoucherCode;
    private BigDecimal totalPrice;
    private PaymentOption paymentOption;
    private BigDecimal requiredPaymentAmount;
    private BigDecimal paidAmount;
    private String paymentUrl;
    private String invoiceNumber;
    private BookingStatus status;

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public Integer getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(Integer roomNumber) {
        this.roomNumber = roomNumber;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public String getCustomerFullName() {
        return customerFullName;
    }

    public void setCustomerFullName(String customerFullName) {
        this.customerFullName = customerFullName;
    }

    public RentalMode getRentalMode() {
        return rentalMode;
    }

    public void setRentalMode(RentalMode rentalMode) {
        this.rentalMode = rentalMode;
    }

    public LocalDateTime getCheckInAt() {
        return checkInAt;
    }

    public void setCheckInAt(LocalDateTime checkInAt) {
        this.checkInAt = checkInAt;
    }

    public LocalDateTime getCheckOutAt() {
        return checkOutAt;
    }

    public void setCheckOutAt(LocalDateTime checkOutAt) {
        this.checkOutAt = checkOutAt;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public PaymentOption getPaymentOption() {
        return paymentOption;
    }

    public void setPaymentOption(PaymentOption paymentOption) {
        this.paymentOption = paymentOption;
    }

    public BigDecimal getRequiredPaymentAmount() {
        return requiredPaymentAmount;
    }

    public void setRequiredPaymentAmount(BigDecimal requiredPaymentAmount) {
        this.requiredPaymentAmount = requiredPaymentAmount;
    }

    public BigDecimal getPaidAmount() {
        return paidAmount;
    }

    public void setPaidAmount(BigDecimal paidAmount) {
        this.paidAmount = paidAmount;
    }

    public String getPaymentUrl() {
        return paymentUrl;
    }

    public void setPaymentUrl(String paymentUrl) {
        this.paymentUrl = paymentUrl;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public BigDecimal getOriginalPrice() {
        return originalPrice;
    }

    public void setOriginalPrice(BigDecimal originalPrice) {
        this.originalPrice = originalPrice;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public String getAppliedVoucherCode() {
        return appliedVoucherCode;
    }

    public void setAppliedVoucherCode(String appliedVoucherCode) {
        this.appliedVoucherCode = appliedVoucherCode;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }
}
