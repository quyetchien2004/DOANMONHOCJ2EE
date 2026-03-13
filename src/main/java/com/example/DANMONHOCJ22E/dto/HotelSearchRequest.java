package com.example.DANMONHOCJ22E.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.springframework.format.annotation.DateTimeFormat;

import com.example.DANMONHOCJ22E.model.RentalMode;

public class HotelSearchRequest {

    private String province;

    private RentalMode rentalMode;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime startDateTime;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime endDateTime;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate endDate;

    private BigDecimal maxPrice;

    private Double userLatitude;

    private Double userLongitude;

    public LocalDateTime resolvedCheckInAt() {
        if (rentalMode == RentalMode.HOURLY) {
            return startDateTime;
        }
        return startDate == null ? null : startDate.atTime(LocalTime.of(14, 0));
    }

    public LocalDateTime resolvedCheckOutAt() {
        if (rentalMode == RentalMode.HOURLY) {
            return endDateTime;
        }
        return endDate == null ? null : endDate.atTime(LocalTime.NOON);
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public RentalMode getRentalMode() {
        return rentalMode;
    }

    public void setRentalMode(RentalMode rentalMode) {
        this.rentalMode = rentalMode;
    }

    public LocalDateTime getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(LocalDateTime startDateTime) {
        this.startDateTime = startDateTime;
    }

    public LocalDateTime getEndDateTime() {
        return endDateTime;
    }

    public void setEndDateTime(LocalDateTime endDateTime) {
        this.endDateTime = endDateTime;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public BigDecimal getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(BigDecimal maxPrice) {
        this.maxPrice = maxPrice;
    }

    public Double getUserLatitude() {
        return userLatitude;
    }

    public void setUserLatitude(Double userLatitude) {
        this.userLatitude = userLatitude;
    }

    public Double getUserLongitude() {
        return userLongitude;
    }

    public void setUserLongitude(Double userLongitude) {
        this.userLongitude = userLongitude;
    }
}
