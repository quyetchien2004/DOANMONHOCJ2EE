package com.example.DANMONHOCJ22E.service;

import com.example.DANMONHOCJ22E.dto.BranchAvailabilityResponse;
import com.example.DANMONHOCJ22E.dto.HotelSearchRequest;
import com.example.DANMONHOCJ22E.dto.RoomAvailabilityResponse;
import com.example.DANMONHOCJ22E.model.BookingStatus;
import com.example.DANMONHOCJ22E.model.HotelBranch;
import com.example.DANMONHOCJ22E.model.RentalMode;
import com.example.DANMONHOCJ22E.model.Room;
import com.example.DANMONHOCJ22E.repository.BookingRepository;
import com.example.DANMONHOCJ22E.repository.HotelBranchRepository;
import com.example.DANMONHOCJ22E.repository.RoomRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class HotelSearchService {

    private final HotelBranchRepository branchRepository;
    private final RoomRepository roomRepository;
    private final BookingRepository bookingRepository;
    private final PricingService pricingService;

    public HotelSearchService(HotelBranchRepository branchRepository,
                              RoomRepository roomRepository,
                              BookingRepository bookingRepository,
                              PricingService pricingService) {
        this.branchRepository = branchRepository;
        this.roomRepository = roomRepository;
        this.bookingRepository = bookingRepository;
        this.pricingService = pricingService;
    }

    public List<BranchAvailabilityResponse> search(HotelSearchRequest request) {
        validateRequest(request);

        LocalDateTime checkIn = request.resolvedCheckInAt();
        LocalDateTime checkOut = request.resolvedCheckOutAt();

        List<HotelBranch> branches = (request.getProvince() == null || request.getProvince().isBlank())
                ? branchRepository.findAll()
                : branchRepository.findByProvinceContainingIgnoreCase(request.getProvince().trim());

        List<BranchAvailabilityResponse> responses = new ArrayList<>();

        for (HotelBranch branch : branches) {
            List<Room> rooms = roomRepository.findByBranchId(branch.getId());
            List<RoomAvailabilityResponse> roomResponses = new ArrayList<>();

            for (Room room : rooms) {
                if (!isRoomAvailable(room.getId(), checkIn, checkOut)) {
                    continue;
                }

                BigDecimal estimatedPrice = pricingService.calculateEstimatedPrice(room, request.getRentalMode(), checkIn, checkOut);

                if (request.getMaxPrice() != null && estimatedPrice.compareTo(request.getMaxPrice()) > 0) {
                    continue;
                }

                RoomAvailabilityResponse roomRes = new RoomAvailabilityResponse();
                roomRes.setRoomId(room.getId());
                roomRes.setFloorNumber(room.getFloorNumber());
                roomRes.setRoomNumber(room.getRoomNumber());
                roomRes.setRoomType(room.getRoomType());
                roomRes.setCapacity(room.getCapacity());
                roomRes.setHasNiceView(room.getHasNiceView());
                roomRes.setHourlyRate(room.getHourlyRate());
                roomRes.setDailyRate(room.getDailyRate());
                roomRes.setEstimatedPrice(estimatedPrice);
                roomResponses.add(roomRes);
            }

            if (roomResponses.isEmpty()) {
                continue;
            }

            roomResponses.sort(Comparator.comparing(RoomAvailabilityResponse::getEstimatedPrice));

            BranchAvailabilityResponse branchRes = new BranchAvailabilityResponse();
            branchRes.setBranchId(branch.getId());
            branchRes.setBranchName(branch.getName());
            branchRes.setProvince(branch.getProvince());
            branchRes.setAddress(branch.getAddress());
            branchRes.setLatitude(branch.getLatitude());
            branchRes.setLongitude(branch.getLongitude());
            branchRes.setTotalFloors(branch.getTotalFloors());
            branchRes.setRoomsPerFloor(branch.getRoomsPerFloor());
            branchRes.setDistanceKm(resolveDistanceKm(request, branch));
            branchRes.setAvailableRooms(roomResponses);

            responses.add(branchRes);
        }

        responses.sort(Comparator
            .comparing((BranchAvailabilityResponse b) -> Optional.ofNullable(b.getDistanceKm()).orElse(Double.MAX_VALUE))
                .thenComparing(BranchAvailabilityResponse::getBranchName));

        return responses;
    }

    private void validateRequest(HotelSearchRequest request) {
        if (request.getRentalMode() == null) {
            throw new IllegalArgumentException("rentalMode la bat buoc");
        }

        LocalDateTime checkIn = request.resolvedCheckInAt();
        LocalDateTime checkOut = request.resolvedCheckOutAt();

        if (checkIn == null || checkOut == null) {
            throw new IllegalArgumentException("Thoi gian check-in/check-out khong hop le");
        }

        if (!checkOut.isAfter(checkIn)) {
            throw new IllegalArgumentException("checkOut phai sau checkIn");
        }

        if (request.getRentalMode() == RentalMode.DAILY) {
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

    private boolean isRoomAvailable(Long roomId, LocalDateTime checkIn, LocalDateTime checkOut) {
        return bookingRepository.findConflictingBookings(
                roomId,
                checkIn,
                checkOut,
                List.of(BookingStatus.PENDING, BookingStatus.CONFIRMED)
        ).isEmpty();
    }

    private Double resolveDistanceKm(HotelSearchRequest request, HotelBranch branch) {
        if (request.getUserLatitude() == null || request.getUserLongitude() == null) {
            return null;
        }
        return haversineKm(
                request.getUserLatitude(),
                request.getUserLongitude(),
                branch.getLatitude(),
                branch.getLongitude()
        );
    }

    private double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return BigDecimal.valueOf(6371.0 * c).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
