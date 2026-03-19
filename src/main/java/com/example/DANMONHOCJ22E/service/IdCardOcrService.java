package com.example.DANMONHOCJ22E.service;

import java.util.Base64;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
public class IdCardOcrService {

    @Value("${app.ocr.space.url}")
    private String ocrApiUrl;

    @Value("${app.ocr.space.api-key:}")
    private String ocrApiKey;

    @SuppressWarnings({"unchecked", "null"})
    public String extractFullName(byte[] fileBytes, String originalFilename) {
        if (ocrApiKey == null || ocrApiKey.isBlank()) {
            throw new IllegalArgumentException("Chua cau hinh app.ocr.space.api-key de xac minh CCCD");
        }
        if (ocrApiUrl == null || ocrApiUrl.isBlank()) {
            throw new IllegalArgumentException("Chua cau hinh app.ocr.space.url");
        }
        if (fileBytes == null || fileBytes.length == 0) {
            throw new IllegalArgumentException("Anh CCCD khong hop le");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("apikey", ocrApiKey);
        body.add("language", "auto");
        body.add("isOverlayRequired", "false");
        body.add("OCREngine", "3");
        body.add("scale", "true");
        body.add("detectOrientation", "true");
        body.add("base64Image", "data:" + resolveMimeType(originalFilename) + ";base64," + Base64.getEncoder().encodeToString(fileBytes));

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(body, headers);
        ResponseEntity<Map<String, Object>> response = new RestTemplate().postForEntity(
                ocrApiUrl,
                requestEntity,
                (Class<Map<String, Object>>) (Class<?>) Map.class
        );

        Map<String, Object> payload = response.getBody();
        if (payload == null) {
            throw new IllegalArgumentException("Khong doc duoc ket qua OCR");
        }

        Object isErrored = payload.get("IsErroredOnProcessing");
        if (Boolean.TRUE.equals(isErrored)) {
            throw new IllegalArgumentException("OCR loi: " + extractErrorMessage(payload));
        }

        Object parsedResults = payload.get("ParsedResults");
        if (!(parsedResults instanceof List<?> list) || list.isEmpty()) {
            throw new IllegalArgumentException("OCR khong tra ve van ban hop le. " + extractErrorMessage(payload));
        }

        Object first = list.get(0);
        if (!(first instanceof Map<?, ?> map)) {
            throw new IllegalArgumentException("OCR tra ve du lieu khong hop le");
        }

        Object parsedText = map.get("ParsedText");
        if (parsedText == null) {
            throw new IllegalArgumentException("Khong trich xuat duoc thong tin tu CCCD");
        }

        return parsedText.toString();
    }

    @SuppressWarnings("unchecked")
    private String extractErrorMessage(Map<String, Object> payload) {
        Object errorMessage = payload.get("ErrorMessage");
        if (errorMessage instanceof List<?> messages && !messages.isEmpty()) {
            return String.valueOf(messages.get(0));
        }
        if (errorMessage != null) {
            return errorMessage.toString();
        }

        Object details = payload.get("ErrorDetails");
        if (details != null && !details.toString().isBlank()) {
            return details.toString();
        }

        Object parsedResults = payload.get("ParsedResults");
        if (parsedResults instanceof List<?> list && !list.isEmpty() && list.get(0) instanceof Map<?, ?> map) {
            Object localError = ((Map<String, Object>) map).get("ErrorMessage");
            if (localError != null && !localError.toString().isBlank()) {
                return localError.toString();
            }
        }

        return "Vui long kiem tra API key, dung luong anh, va chat luong anh CCCD";
    }

    private String resolveMimeType(String filename) {
        if (filename == null) {
            return "image/jpeg";
        }
        String lowered = filename.toLowerCase();
        if (lowered.endsWith(".png")) {
            return "image/png";
        }
        if (lowered.endsWith(".webp")) {
            return "image/webp";
        }
        return "image/jpeg";
    }
}
