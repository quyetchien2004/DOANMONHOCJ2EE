package com.example.DANMONHOCJ22E.controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.DANMONHOCJ22E.model.HotelBranch;
import com.example.DANMONHOCJ22E.model.Room;
import com.example.DANMONHOCJ22E.model.RoomType;
import com.example.DANMONHOCJ22E.model.Voucher;
import com.example.DANMONHOCJ22E.model.VoucherAudience;
import com.example.DANMONHOCJ22E.repository.HotelBranchRepository;
import com.example.DANMONHOCJ22E.repository.RoomRepository;
import com.example.DANMONHOCJ22E.repository.VoucherRepository;

@Controller
@RequestMapping("/admin")
public class AdminManagementController {

    private final HotelBranchRepository branchRepository;
    private final RoomRepository roomRepository;
    private final VoucherRepository voucherRepository;

    public AdminManagementController(HotelBranchRepository branchRepository,
                                     RoomRepository roomRepository,
                                     VoucherRepository voucherRepository) {
        this.branchRepository = branchRepository;
        this.roomRepository = roomRepository;
        this.voucherRepository = voucherRepository;
    }

    @GetMapping("/manage")
    public String manage(@RequestParam(required = false) Long branchId,
                         @RequestParam(name = "roomPage", defaultValue = "1") Integer roomPage,
                         @RequestParam(name = "roomSize", defaultValue = "8") Integer roomSize,
                         Model model) {
        List<HotelBranch> branches = branchRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(HotelBranch::getName))
                .toList();

        Long selectedBranchId = branchId;
        if (selectedBranchId == null && !branches.isEmpty()) {
            selectedBranchId = branches.get(0).getId();
        }

        List<Room> allRooms = selectedBranchId == null
                ? List.of()
                : roomRepository.findByBranchId(selectedBranchId)
                .stream()
                .sorted(Comparator.comparing(Room::getRoomNumber))
                .toList();

        int resolvedRoomSize = (roomSize == null || roomSize < 1) ? 8 : Math.min(roomSize, 50);
        int totalRooms = allRooms.size();
        int totalRoomPages = totalRooms == 0 ? 1 : (int) Math.ceil((double) totalRooms / resolvedRoomSize);
        int resolvedRoomPage = roomPage == null ? 1 : roomPage;
        if (resolvedRoomPage < 1) {
            resolvedRoomPage = 1;
        }
        if (resolvedRoomPage > totalRoomPages) {
            resolvedRoomPage = totalRoomPages;
        }

        int fromIndex = (resolvedRoomPage - 1) * resolvedRoomSize;
        int toIndex = Math.min(fromIndex + resolvedRoomSize, totalRooms);
        List<Room> pagedRooms = totalRooms == 0 ? List.of() : allRooms.subList(fromIndex, toIndex);

        model.addAttribute("branches", branches);
        model.addAttribute("selectedBranchId", selectedBranchId);
        model.addAttribute("rooms", pagedRooms);
        model.addAttribute("totalRooms", totalRooms);
        model.addAttribute("currentRoomPage", resolvedRoomPage);
        model.addAttribute("totalRoomPages", totalRoomPages);
        model.addAttribute("roomPageSize", resolvedRoomSize);
        model.addAttribute("roomTypes", RoomType.values());
        model.addAttribute("voucherAudiences", VoucherAudience.values());
        model.addAttribute("vouchers", voucherRepository.findAll().stream().sorted(Comparator.comparing(Voucher::getCode)).toList());
        return "admin-manage";
    }

    @PostMapping("/branches/save")
    public String saveBranch(@RequestParam(required = false) Long id,
                             @RequestParam String name,
                             @RequestParam String province,
                             @RequestParam String address,
                             @RequestParam Double latitude,
                             @RequestParam Double longitude,
                             @RequestParam Integer totalFloors,
                             @RequestParam Integer roomsPerFloor) {
        HotelBranch b = (id == null)
                ? new HotelBranch()
                : branchRepository.findById(id).orElse(new HotelBranch());

        b.setName(name.trim());
        b.setProvince(province.trim());
        b.setAddress(address.trim());
        b.setLatitude(latitude);
        b.setLongitude(longitude);
        b.setTotalFloors(totalFloors);
        b.setRoomsPerFloor(roomsPerFloor);

        branchRepository.save(b);
        return "redirect:/admin/manage?branchId=" + b.getId();
    }

    @PostMapping("/branches/{id}/delete")
    public String deleteBranch(@PathVariable Long id) {
        branchRepository.deleteById(Objects.requireNonNull(id));
        return "redirect:/admin/manage";
    }

    @PostMapping("/rooms/save")
    public String saveRoom(@RequestParam(required = false) Long id,
                           @RequestParam Long branchId,
                           @RequestParam Integer floorNumber,
                           @RequestParam Integer roomNumber,
                           @RequestParam RoomType roomType,
                           @RequestParam Integer capacity,
                           @RequestParam BigDecimal hourlyRate,
                           @RequestParam BigDecimal dailyRate,
                           @RequestParam(defaultValue = "false") Boolean hasNiceView) {

        Long requiredBranchId = Objects.requireNonNull(branchId);
        HotelBranch branch = branchRepository.findById(requiredBranchId)
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay branch"));

        Room room = (id == null)
                ? new Room()
                : roomRepository.findById(id).orElse(new Room());

        room.setBranch(branch);
        room.setFloorNumber(floorNumber);
        room.setRoomNumber(roomNumber);
        room.setRoomType(roomType);
        room.setCapacity(capacity);
        room.setHourlyRate(hourlyRate);
        room.setDailyRate(dailyRate);
        room.setHasNiceView(hasNiceView);
        roomRepository.save(room);

        return "redirect:/admin/manage?branchId=" + branchId;
    }

    @PostMapping("/rooms/{id}/delete")
    public String deleteRoom(@PathVariable Long id, @RequestParam Long branchId) {
        roomRepository.deleteById(Objects.requireNonNull(id));
        return "redirect:/admin/manage?branchId=" + branchId;
    }

    @PostMapping("/vouchers/save")
    public String saveVoucher(@RequestParam(required = false) Long id,
                              @RequestParam String code,
                              @RequestParam String name,
                              @RequestParam VoucherAudience audience,
                              @RequestParam BigDecimal discountPercent,
                              @RequestParam(defaultValue = "false") Boolean active,
                              @RequestParam(required = false) String validFrom,
                              @RequestParam(required = false) String validTo,
                              @RequestParam(required = false) Long branchId) {
        Voucher voucher = (id == null)
                ? new Voucher()
                : voucherRepository.findById(id).orElse(new Voucher());

        voucher.setCode(code.trim().toUpperCase());
        voucher.setName(name.trim());
        voucher.setAudience(audience);
        voucher.setDiscountPercent(discountPercent);
        voucher.setActive(active);
        voucher.setValidFrom((validFrom == null || validFrom.isBlank()) ? null : LocalDateTime.parse(validFrom));
        voucher.setValidTo((validTo == null || validTo.isBlank()) ? null : LocalDateTime.parse(validTo));
        voucherRepository.save(voucher);

        if (branchId != null) {
            return "redirect:/admin/manage?branchId=" + branchId;
        }
        return "redirect:/admin/manage";
    }

    @PostMapping("/vouchers/{id}/delete")
    public String deleteVoucher(@PathVariable Long id, @RequestParam(required = false) Long branchId) {
        voucherRepository.deleteById(Objects.requireNonNull(id));
        if (branchId != null) {
            return "redirect:/admin/manage?branchId=" + branchId;
        }
        return "redirect:/admin/manage";
    }
}
