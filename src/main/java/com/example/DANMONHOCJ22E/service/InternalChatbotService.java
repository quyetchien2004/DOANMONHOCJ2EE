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
            response.put("answer", "Đặt phòng có hiệu lực sau khi thanh toán VNPAY thành công. Bạn có thể chọn thanh toán 100% hoặc cọc 30% giá trị đơn.");
            response.put("topic", "payment");
            return response;
        }

        if (q.contains("voucher") || q.contains("giam gia")) {
            response.put("answer", "Hệ thống có voucher 10% cho tài khoản mới và khi hoàn tất booking thành công lần 2. Voucher 25% được tặng khi tài khoản xác minh CCCD và hoàn tất booking đầu tiên thành công (trust 100%).");
            response.put("topic", "voucher");
            return response;
        }

        if (q.contains("cccd") || q.contains("xac minh") || q.contains("do tin cay") || q.contains("trust")) {
            response.put("answer", "Vào mục Tài khoản của bạn để tải CCCD. Nếu OCR xác minh tên khớp tài khoản, trust sẽ lên 80%. Sau booking thành công đầu tiên, trust sẽ lên 100%.");
            response.put("topic", "verification");
            return response;
        }

        if ((q.contains("con phong") || q.contains("het phong") || q.contains("trong phong") || q.contains("availability") || q.contains("available"))
                && !q.contains("dat phong")) {
            response.put("answer", "Để kiểm tra còn phòng, bạn vào trang Đặt phòng, chọn ngày/khung giờ và bấm Tìm phòng. Hệ thống sẽ trả về danh sách phòng còn trống theo thời gian bạn chọn.");
            response.put("topic", "availability");
            Map<String, Object> searchGuide = new HashMap<>();
            searchGuide.put("api", "GET /api/hotels/search");
            searchGuide.put("required", new String[]{"rentalMode", "startDate/startDateTime", "endDate/endDateTime"});
            searchGuide.put("note", "Chỉ có kết quả còn phòng khi bạn chọn khoảng thời gian cụ thể");
            response.put("searchGuide", searchGuide);
            return response;
        }

        if (q.contains("dat phong") || q.contains("book")) {
            response.put("answer", "Bạn vào trang Đặt phòng, chọn thời gian, phòng, hình thức thanh toán (100% hoặc cọc 30%) và xác nhận thanh toán VNPAY để kích hoạt đơn.");
            response.put("topic", "booking");
            Map<String, Object> bookingGuide = new HashMap<>();
            bookingGuide.put("api", "POST /api/bookings");
            bookingGuide.put("required", new String[]{"roomId", "rentalMode", "customerFullName", "paymentOption"});
            bookingGuide.put("optional", new String[]{"voucherCode", "startDate/startDateTime", "endDate/endDateTime"});
            response.put("bookingGuide", bookingGuide);
            return response;
        }

        response.put("topic", "general");
        response.put("answer", "Tôi là chatbot nội bộ của CCT Hotels Company. Tôi hỗ trợ các chủ đề: đặt phòng, thanh toán VNPAY, voucher, xác minh CCCD, tài khoản và đơn đặt phòng.");
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



