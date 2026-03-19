package com.example.DANMONHOCJ22E.service;

import java.text.Normalizer;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.springframework.stereotype.Service;

@Service
public class InternalChatbotService {

    public Map<String, Object> ask(String message) {
        String q = normalize(message);
        Map<String, Object> response = new HashMap<>();
        response.put("question", message);

        if (q.contains("vnpay") || q.contains("thanh toan") || q.contains("chuyen khoan")) {
            response.put("answer", "Dat phong co hieu luc sau khi thanh toan VNPAY thanh cong. Ban co the chon thanh toan 100% hoac coc 30% gia tri don.");
            response.put("topic", "payment");
            return response;
        }

        if (q.contains("voucher") || q.contains("giam gia")) {
            response.put("answer", "He thong co voucher 10% cho tai khoan moi va khi hoan tat booking thanh cong lan 2. Voucher 25% duoc tang khi tai khoan xac minh CCCD va hoan tat booking dau tien thanh cong (trust 100%).");
            response.put("topic", "voucher");
            return response;
        }

        if (q.contains("cccd") || q.contains("xac minh") || q.contains("do tin cay") || q.contains("trust")) {
            response.put("answer", "Vao muc Tai khoan cua ban de upload CCCD. Neu OCR xac minh ten khop tai khoan, trust se len 80%. Sau booking thanh cong dau tien, trust se len 100%.");
            response.put("topic", "verification");
            return response;
        }

        if ((q.contains("con phong") || q.contains("het phong") || q.contains("trong phong") || q.contains("availability") || q.contains("available"))
                && !q.contains("dat phong")) {
            response.put("answer", "De kiem tra con phong, ban vao trang Dat phong, chon ngay/khung gio va bam Tim phong. He thong se tra ve danh sach phong con trong theo thoi gian ban chon.");
            response.put("topic", "availability");
            Map<String, Object> searchGuide = new HashMap<>();
            searchGuide.put("api", "GET /api/hotels/search");
            searchGuide.put("required", new String[]{"rentalMode", "startDate/startDateTime", "endDate/endDateTime"});
            searchGuide.put("note", "Chi co ket qua con phong khi ban chon khoang thoi gian cu the");
            response.put("searchGuide", searchGuide);
            return response;
        }

        if (q.contains("dat phong") || q.contains("book")) {
            response.put("answer", "Ban vao trang Dat phong, chon thoi gian, phong, hinh thuc thanh toan (100% hoac coc 30%) va xac nhan thanh toan VNPAY de kich hoat don.");
            response.put("topic", "booking");
            Map<String, Object> bookingGuide = new HashMap<>();
            bookingGuide.put("api", "POST /api/bookings");
            bookingGuide.put("required", new String[]{"roomId", "rentalMode", "customerFullName", "paymentOption"});
            bookingGuide.put("optional", new String[]{"voucherCode", "startDate/startDateTime", "endDate/endDateTime"});
            response.put("bookingGuide", bookingGuide);
            return response;
        }

        response.put("topic", "general");
        response.put("answer", "Toi la chatbot noi bo Golden Lotus. Toi ho tro cac chu de: dat phong, thanh toan VNPAY, voucher, xac minh CCCD, tai khoan va don dat phong.");
        return response;
    }

    private String normalize(String message) {
        if (message == null) {
            return "";
        }
        String lowered = message.trim().toLowerCase(Locale.ROOT);
        String normalized = Normalizer.normalize(lowered, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replace('đ', 'd');
        return normalized.replaceAll("\\s+", " ");
    }
}
