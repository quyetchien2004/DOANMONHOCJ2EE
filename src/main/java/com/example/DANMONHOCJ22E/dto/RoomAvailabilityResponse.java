package com.example.DANMONHOCJ22E.dto;

import com.example.DANMONHOCJ22E.model.RoomType;

import java.math.BigDecimal;

public class RoomAvailabilityResponse {

    private Long roomId;
    private Integer floorNumber;
    private Integer roomNumber;
    private RoomType roomType;
    private Integer capacity;
    private Boolean hasNiceView;
    private BigDecimal hourlyRate;
    private BigDecimal dailyRate;
    private BigDecimal estimatedPrice;

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public Integer getFloorNumber() {
        return floorNumber;
    }

    public void setFloorNumber(Integer floorNumber) {
        this.floorNumber = floorNumber;
    }

    public Integer getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(Integer roomNumber) {
        this.roomNumber = roomNumber;
    }

    public RoomType getRoomType() {
        return roomType;
    }

    public void setRoomType(RoomType roomType) {
        this.roomType = roomType;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public Boolean getHasNiceView() {
        return hasNiceView;
    }

    public void setHasNiceView(Boolean hasNiceView) {
        this.hasNiceView = hasNiceView;
    }

    public BigDecimal getHourlyRate() {
        return hourlyRate;
    }

    public void setHourlyRate(BigDecimal hourlyRate) {
        this.hourlyRate = hourlyRate;
    }

    public BigDecimal getDailyRate() {
        return dailyRate;
    }

    public void setDailyRate(BigDecimal dailyRate) {
        this.dailyRate = dailyRate;
    }

    public BigDecimal getEstimatedPrice() {
        return estimatedPrice;
    }

    public void setEstimatedPrice(BigDecimal estimatedPrice) {
        this.estimatedPrice = estimatedPrice;
    }
}
