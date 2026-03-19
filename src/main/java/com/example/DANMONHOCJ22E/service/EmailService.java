package com.example.DANMONHOCJ22E.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendOtp(String to, String otpCode) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Ma OTP doi mat khau - Golden Lotus");
        message.setText("Ma OTP cua ban la: " + otpCode + "\nMa co hieu luc trong 10 phut.");
        mailSender.send(message);
    }
}
