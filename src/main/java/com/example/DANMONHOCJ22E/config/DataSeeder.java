package com.example.DANMONHOCJ22E.config;

import com.example.DANMONHOCJ22E.model.HotelBranch;
import com.example.DANMONHOCJ22E.model.Room;
import com.example.DANMONHOCJ22E.model.RoomType;
import com.example.DANMONHOCJ22E.repository.HotelBranchRepository;
import com.example.DANMONHOCJ22E.repository.RoomRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {

    private final HotelBranchRepository branchRepository;
    private final RoomRepository roomRepository;

    public DataSeeder(HotelBranchRepository branchRepository, RoomRepository roomRepository) {
        this.branchRepository = branchRepository;
        this.roomRepository = roomRepository;
    }

    @Override
    public void run(String... args) {
        if (branchRepository.count() > 0 || roomRepository.count() > 0) {
            return;
        }

        List<HotelBranch> branches = new ArrayList<>();
        branches.add(createBranch("CCT Hotels Company Ha Noi", "Ha Noi", "12 Pho Hue, Hai Ba Trung", 21.0181, 105.8558, 8, 10));
        branches.add(createBranch("CCT Hotels Company Da Nang", "Da Nang", "24 Vo Nguyen Giap, Son Tra", 16.0667, 108.2390, 10, 10));
        branches.add(createBranch("CCT Hotels Company Sai Gon", "TP Ho Chi Minh", "56 Nguyen Hue, Quan 1", 10.7757, 106.7004, 12, 10));

        branchRepository.saveAll(branches);

        List<Room> allRooms = new ArrayList<>();
        for (HotelBranch branch : branches) {
            allRooms.addAll(generateRoomsForBranch(branch));
        }
        roomRepository.saveAll(allRooms);
    }

    private HotelBranch createBranch(String name,
                                     String province,
                                     String address,
                                     double lat,
                                     double lng,
                                     int floors,
                                     int roomsPerFloor) {
        HotelBranch b = new HotelBranch();
        b.setName(name);
        b.setProvince(province);
        b.setAddress(address);
        b.setLatitude(lat);
        b.setLongitude(lng);
        b.setTotalFloors(floors);
        b.setRoomsPerFloor(roomsPerFloor);
        return b;
    }

    private List<Room> generateRoomsForBranch(HotelBranch branch) {
        List<Room> rooms = new ArrayList<>();

        for (int floor = 1; floor <= branch.getTotalFloors(); floor++) {
            for (int idx = 1; idx <= branch.getRoomsPerFloor(); idx++) {
                RoomType roomType = resolveRoomType(idx);
                int roomNumber = floor * 100 + idx;
                boolean niceView = floor >= (branch.getTotalFloors() - 1) && (idx == 1 || idx == 2);

                BigDecimal baseHourly = baseHourlyRate(roomType);
                BigDecimal baseDaily = baseDailyRate(roomType);

                BigDecimal floorFactor = BigDecimal.ONE.subtract(
                        BigDecimal.valueOf((floor - 1) * 0.015)
                );
                if (floorFactor.compareTo(new BigDecimal("0.82")) < 0) {
                    floorFactor = new BigDecimal("0.82");
                }

                BigDecimal hourly = baseHourly.multiply(floorFactor);
                BigDecimal daily = baseDaily.multiply(floorFactor);

                if (niceView) {
                    hourly = hourly.multiply(new BigDecimal("1.20"));
                    daily = daily.multiply(new BigDecimal("1.20"));
                }

                Room room = new Room();
                room.setBranch(branch);
                room.setFloorNumber(floor);
                room.setRoomNumber(roomNumber);
                room.setRoomType(roomType);
                room.setCapacity(capacityByRoomType(roomType));
                room.setHourlyRate(hourly.setScale(2, RoundingMode.HALF_UP));
                room.setDailyRate(daily.setScale(2, RoundingMode.HALF_UP));
                room.setHasNiceView(niceView);

                rooms.add(room);
            }
        }

        return rooms;
    }

    private RoomType resolveRoomType(int idx) {
        if (idx <= 3) {
            return RoomType.SINGLE;
        }
        if (idx <= 6) {
            return RoomType.DOUBLE;
        }
        if (idx <= 8) {
            return RoomType.TRIPLE;
        }
        return RoomType.FAMILY;
    }

    private int capacityByRoomType(RoomType type) {
        return switch (type) {
            case SINGLE -> 1;
            case DOUBLE -> 2;
            case TRIPLE -> 3;
            case FAMILY -> 4;
        };
    }

    private BigDecimal baseHourlyRate(RoomType type) {
        return switch (type) {
            case SINGLE -> new BigDecimal("60000");
            case DOUBLE -> new BigDecimal("80000");
            case TRIPLE -> new BigDecimal("105000");
            case FAMILY -> new BigDecimal("140000");
        };
    }

    private BigDecimal baseDailyRate(RoomType type) {
        return switch (type) {
            case SINGLE -> new BigDecimal("450000");
            case DOUBLE -> new BigDecimal("650000");
            case TRIPLE -> new BigDecimal("850000");
            case FAMILY -> new BigDecimal("1200000");
        };
    }
}

