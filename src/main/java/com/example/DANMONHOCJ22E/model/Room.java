package com.example.DANMONHOCJ22E.model;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "rooms", indexes = {
        @Index(name = "idx_room_branch", columnList = "branch_id"),
        @Index(name = "idx_room_branch_number", columnList = "branch_id,roomNumber", unique = true)
})
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "branch_id", nullable = false)
    private HotelBranch branch;

    @Column(nullable = false)
    private Integer floorNumber;

    @Column(nullable = false)
    private Integer roomNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RoomType roomType;

    @Column(nullable = false)
    private Integer capacity;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal hourlyRate;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal dailyRate;

    @Column(nullable = false)
    private Boolean hasNiceView = false;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public HotelBranch getBranch() {
        return branch;
    }

    public void setBranch(HotelBranch branch) {
        this.branch = branch;
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

    public Boolean getHasNiceView() {
        return hasNiceView;
    }

    public void setHasNiceView(Boolean hasNiceView) {
        this.hasNiceView = hasNiceView;
    }
}
