package com.example.DANMONHOCJ22E.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.DANMONHOCJ22E.model.Booking;
import com.example.DANMONHOCJ22E.service.BookingStatusHelper;
import com.example.DANMONHOCJ22E.service.PaymentService;

@Controller
@RequestMapping("/admin/bookings")
public class AdminBookingController {

    private final PaymentService paymentService;

    public AdminBookingController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping
    public String bookingApprovalPage(Model model) {
        List<Booking> pendingApprovals = paymentService.getPendingDepositApprovals();
        model.addAttribute("pendingApprovals", pendingApprovals);
        model.addAttribute("statusHelper", BookingStatusHelper.class);
        return "admin-bookings";
    }

    @PostMapping("/{id}/approve")
    public String approve(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Booking booking = paymentService.approvePendingDeposit(id);
        redirectAttributes.addFlashAttribute("message", "Da duyet don #" + booking.getId() + " thanh cong");
        return "redirect:/admin/bookings";
    }

    @PostMapping("/{id}/reject")
    public String reject(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Booking booking = paymentService.rejectPendingDeposit(id);
        redirectAttributes.addFlashAttribute("message", "Da tu choi don #" + booking.getId());
        return "redirect:/admin/bookings";
    }
}
