package com.example.DANMONHOCJ22E.service;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class VnPayService {

    @Value("${vnpay.tmn-code:DEMO}")
    private String tmnCode;

    @Value("${vnpay.hash-secret:DEMO_SECRET}")
    private String hashSecret;

    @Value("${vnpay.pay-url:https://sandbox.vnpayment.vn/paymentv2/vpcpay.html}")
    private String payUrl;

    @Value("${vnpay.return-url:http://localhost:8080/api/payments/vnpay/callback}")
    private String returnUrl;

    public String createPaymentUrl(String txnRef, BigDecimal amount, String ipAddress, String orderInfo) {
        long vnpAmount = amount.multiply(new BigDecimal("100")).longValue();
        String createDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

        Map<String, String> params = new HashMap<>();
        params.put("vnp_Version", "2.1.0");
        params.put("vnp_Command", "pay");
        params.put("vnp_TmnCode", tmnCode);
        params.put("vnp_Amount", String.valueOf(vnpAmount));
        params.put("vnp_CurrCode", "VND");
        params.put("vnp_TxnRef", txnRef);
        params.put("vnp_OrderInfo", orderInfo);
        params.put("vnp_OrderType", "billpayment");
        params.put("vnp_Locale", "vn");
        params.put("vnp_ReturnUrl", returnUrl);
        params.put("vnp_IpAddr", ipAddress == null || ipAddress.isBlank() ? "127.0.0.1" : ipAddress);
        params.put("vnp_CreateDate", createDate);

        String hashData = buildHashData(params);
        String secureHash = hmacSha512(hashSecret, hashData);

        if (payUrl == null || payUrl.isBlank()) {
            throw new IllegalArgumentException("Chua cau hinh vnpay.pay-url");
        }

        List<String> keys = new ArrayList<>(params.keySet());
        keys.sort(Comparator.naturalOrder());

        StringBuilder queryBuilder = new StringBuilder();
        for (String key : keys) {
            String value = params.get(key);
            if (value != null) {
                if (queryBuilder.length() > 0) {
                    queryBuilder.append('&');
                }
                queryBuilder
                        .append(encode(key))
                        .append('=')
                        .append(encode(value));
            }
        }

        if (queryBuilder.length() > 0) {
            queryBuilder.append('&');
        }
        queryBuilder.append("vnp_SecureHash=").append(secureHash);
        return payUrl + "?" + queryBuilder;
    }

    public boolean isValidCallback(Map<String, String> callbackParams) {
        String receivedHash = callbackParams.get("vnp_SecureHash");
        if (receivedHash == null || receivedHash.isBlank()) {
            return false;
        }
        Map<String, String> clone = new HashMap<>(callbackParams);
        clone.remove("vnp_SecureHash");
        clone.remove("vnp_SecureHashType");
        String expected = hmacSha512(hashSecret, buildHashData(clone));
        return expected.equalsIgnoreCase(receivedHash);
    }

    private String buildHashData(Map<String, String> params) {
        List<String> keys = new ArrayList<>(params.keySet());
        keys.sort(Comparator.naturalOrder());

        StringBuilder sb = new StringBuilder();
        for (String key : keys) {
            String value = params.get(key);
            if (value == null || value.isBlank()) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append('&');
            }
            sb.append(encode(key)).append('=').append(encode(value));
        }
        return sb.toString();
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private String hmacSha512(String key, String data) {
        try {
            Mac hmac = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            hmac.init(secretKey);
            byte[] hash = hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                String h = Integer.toHexString(0xff & b);
                if (h.length() == 1) {
                    hex.append('0');
                }
                hex.append(h);
            }
            return hex.toString();
        } catch (GeneralSecurityException ex) {
            throw new IllegalArgumentException("Khong the tao secure hash VNPAY", ex);
        }
    }
}
