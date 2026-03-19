package com.example.DANMONHOCJ22E.controller;

import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.DANMONHOCJ22E.model.Booking;
import com.example.DANMONHOCJ22E.service.PaymentService;

@Controller
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping("/vnpay/callback")
    public String vnpayCallback(@RequestParam Map<String, String> params, RedirectAttributes redirectAttributes) {
        Booking booking = paymentService.processVnPayCallback(params);

        redirectAttributes.addAttribute("bookingId", booking.getId());
        redirectAttributes.addAttribute("status", booking.getStatus().name());
        redirectAttributes.addAttribute("totalPrice", booking.getTotalPrice());
        redirectAttributes.addAttribute("requiredPaymentAmount", booking.getRequiredPaymentAmount());
        redirectAttributes.addAttribute("paidAmount", booking.getPaidAmount());
        redirectAttributes.addAttribute("invoiceNumber", booking.getInvoiceNumber());
        return "redirect:/payment-result";
    }
}
