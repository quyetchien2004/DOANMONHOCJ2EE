package com.example.DANMONHOCJ22E.dto;

import java.util.List;

public class BranchAvailabilityResponse {

    private Long branchId;
    private String branchName;
    private String province;
    private String address;
    private Double latitude;
    private Double longitude;
    private Double distanceKm;
    private Integer totalFloors;
    private Integer roomsPerFloor;
    private List<RoomAvailabilityResponse> availableRooms;

    public Long getBranchId() {
        return branchId;
    }

    public void setBranchId(Long branchId) {
        this.branchId = branchId;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(Double distanceKm) {
        this.distanceKm = distanceKm;
    }

    public Integer getTotalFloors() {
        return totalFloors;
    }

    public void setTotalFloors(Integer totalFloors) {
        this.totalFloors = totalFloors;
    }

    public Integer getRoomsPerFloor() {
        return roomsPerFloor;
    }

    public void setRoomsPerFloor(Integer roomsPerFloor) {
        this.roomsPerFloor = roomsPerFloor;
    }

    public List<RoomAvailabilityResponse> getAvailableRooms() {
        return availableRooms;
    }

    public void setAvailableRooms(List<RoomAvailabilityResponse> availableRooms) {
        this.availableRooms = availableRooms;
    }
}
