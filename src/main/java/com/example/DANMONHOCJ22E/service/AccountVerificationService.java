package com.example.DANMONHOCJ22E.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.DANMONHOCJ22E.model.AccountVerificationStatus;
import com.example.DANMONHOCJ22E.model.BookingStatus;
import com.example.DANMONHOCJ22E.model.User;
import com.example.DANMONHOCJ22E.repository.BookingRepository;
import com.example.DANMONHOCJ22E.repository.UserRepository;

@Service
public class AccountVerificationService {

    private static final long MAX_FILE_SIZE_BYTES = 5L * 1024 * 1024;

    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final IdCardOcrService idCardOcrService;
    private final UserVoucherService userVoucherService;

    @Value("${app.upload.cccd-dir:uploads/cccd}")
    private String cccdUploadDir;

    public AccountVerificationService(UserRepository userRepository,
                                      BookingRepository bookingRepository,
                                      IdCardOcrService idCardOcrService,
                                      UserVoucherService userVoucherService) {
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
        this.idCardOcrService = idCardOcrService;
        this.userVoucherService = userVoucherService;
    }

    @Transactional
    public User uploadAndVerifyIdCard(String username, MultipartFile cccdImage) {
        if (cccdImage == null || cccdImage.isEmpty()) {
            throw new IllegalArgumentException("Anh CCCD khong duoc de trong");
        }
        if (cccdImage.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new IllegalArgumentException("Anh CCCD vuot 5MB");
        }

        String originalFilename = cccdImage.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new IllegalArgumentException("Ten tep khong hop le");
        }

        String loweredName = originalFilename.toLowerCase(Locale.ROOT);
        boolean isAllowedExtension = loweredName.endsWith(".jpg")
                || loweredName.endsWith(".jpeg")
                || loweredName.endsWith(".png");
        if (!isAllowedExtension) {
            throw new IllegalArgumentException("Chi cho phep anh JPG, JPEG hoac PNG");
        }

        String contentType = cccdImage.getContentType();
        if (contentType == null ||
                (!contentType.equalsIgnoreCase("image/jpeg") && !contentType.equalsIgnoreCase("image/png"))) {
            throw new IllegalArgumentException("Tep tai len phai la anh JPG, JPEG hoac PNG");
        }

        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new IllegalArgumentException("Khong tim thay tai khoan");
        }

        String ocrText;
        try {
            ocrText = idCardOcrService.extractFullName(cccdImage.getBytes(), originalFilename);
        } catch (IOException ex) {
            throw new IllegalArgumentException("Khong doc duoc tep CCCD", ex);
        }
        String extractedNameCandidate = extractBestNameCandidate(ocrText, user.getFullName());

        String storedPath = storeFile(cccdImage, user.getId());

        if (!isNameMatched(user.getFullName(), extractedNameCandidate)) {
            throw new IllegalArgumentException("Ho ten tren CCCD khong khop voi tai khoan. OCR doc duoc gan dung: " + abbreviate(extractedNameCandidate));
        }

        user.setIdCardImagePath(storedPath);
        user.setIdCardExtractedFullName(limitLength(extractedNameCandidate, 120));
        user.setAccountVerificationStatus(AccountVerificationStatus.VERIFIED.name());
        user.setIdCardVerifiedAt(LocalDateTime.now());

        long successfulBookingCount = bookingRepository.countByUserAndStatus(user, BookingStatus.CONFIRMED);
        if (successfulBookingCount > 0) {
            user.setTrustScore(100);
            userVoucherService.issueIfMissing(user,
                    UserVoucherService.REASON_TRUST_100_25,
                    UserVoucherService.CODE_FREQUENT25,
                    new BigDecimal("25"),
                    90);
        } else {
            user.setTrustScore(80);
        }

        return userRepository.save(user);
    }

    private String storeFile(MultipartFile file, Long userId) {
        try {
            Path root = Paths.get(cccdUploadDir).toAbsolutePath().normalize();
            Files.createDirectories(root);

            String original = Objects.requireNonNullElse(file.getOriginalFilename(), "cccd.jpg");
            String ext = original.contains(".") ? original.substring(original.lastIndexOf('.')) : ".jpg";
            String filename = "cccd-" + userId + "-" + UUID.randomUUID() + ext;
            Path target = root.resolve(filename);

            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            return target.toString();
        } catch (IOException ex) {
            throw new IllegalArgumentException("Khong luu duoc anh CCCD", ex);
        }
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replace('đ', 'd')
                .replace('Đ', 'D');
        return normalized.replaceAll("[^a-zA-Z0-9]", "").toUpperCase(Locale.ROOT);
    }

    private String normalizeWithSpaces(String value) {
        if (value == null) {
            return "";
        }
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replace('đ', 'd')
                .replace('Đ', 'D');
        return normalized.replaceAll("[^a-zA-Z0-9 ]", " ")
                .replaceAll("\\s+", " ")
                .trim()
                .toUpperCase(Locale.ROOT);
    }

    private boolean isNameMatched(String expectedFullName, String ocrText) {
        String normalizedExpectedCompact = normalize(expectedFullName);
        String normalizedOcrCompact = normalize(ocrText);
        if (!normalizedExpectedCompact.isBlank() && normalizedOcrCompact.contains(normalizedExpectedCompact)) {
            return true;
        }

        String normalizedExpected = normalizeWithSpaces(expectedFullName);
        String normalizedOcr = normalizeWithSpaces(ocrText);
        if (normalizedExpected.isBlank() || normalizedOcr.isBlank()) {
            return false;
        }

        String[] expectedTokens = Arrays.stream(normalizedExpected.split(" "))
                .filter(token -> !token.isBlank())
                .toArray(String[]::new);
        String[] ocrTokens = Arrays.stream(normalizedOcr.split(" "))
                .filter(token -> !token.isBlank())
                .toArray(String[]::new);

        int matchedTokens = 0;
        for (String expectedToken : expectedTokens) {
            if (isTokenMatched(expectedToken, ocrTokens, normalizedOcrCompact)) {
                matchedTokens++;
            }
        }

        if (expectedTokens.length <= 2) {
            return matchedTokens == expectedTokens.length;
        }
        return matchedTokens >= expectedTokens.length - 1;
    }

    private boolean isTokenMatched(String expectedToken, String[] ocrTokens, String normalizedOcrCompact) {
        if (normalizedOcrCompact.contains(expectedToken)) {
            return true;
        }

        int allowedDistance = expectedToken.length() >= 6 ? 2 : 1;
        for (String ocrToken : ocrTokens) {
            if (ocrToken.equals(expectedToken)) {
                return true;
            }
            // Skip expensive Levenshtein when length gap already rules it out
            if (Math.abs(ocrToken.length() - expectedToken.length()) <= allowedDistance
                    && levenshteinDistance(expectedToken, ocrToken, allowedDistance) <= allowedDistance) {
                return true;
            }
        }
        return false;
    }

    /**
     * Levenshtein distance with 1-D rolling array and early-exit when the running
     * minimum for a row already exceeds {@code threshold} – avoids unnecessary work
     * for tokens that will clearly not match.
     */
    private int levenshteinDistance(String left, String right, int threshold) {
        int m = left.length();
        int n = right.length();
        // Length difference alone can never be bridged under the threshold
        if (Math.abs(m - n) > threshold) {
            return threshold + 1;
        }
        int[] prev = new int[n + 1];
        for (int j = 0; j <= n; j++) {
            prev[j] = j;
        }
        for (int i = 1; i <= m; i++) {
            int[] curr = new int[n + 1];
            curr[0] = i;
            int rowMin = i;
            for (int j = 1; j <= n; j++) {
                int cost = left.charAt(i - 1) == right.charAt(j - 1) ? 0 : 1;
                curr[j] = Math.min(Math.min(prev[j] + 1, curr[j - 1] + 1), prev[j - 1] + cost);
                if (curr[j] < rowMin) {
                    rowMin = curr[j];
                }
            }
            // No cell in this row can produce a distance <= threshold – stop early
            if (rowMin > threshold) {
                return threshold + 1;
            }
            prev = curr;
        }
        return prev[n];
    }

    private String abbreviate(String value) {
        if (value == null || value.isBlank()) {
            return "(khong doc duoc van ban)";
        }
        String compact = value.replaceAll("\\s+", " ").trim();
        return compact.length() <= 140 ? compact : compact.substring(0, 140) + "...";
    }

    private String extractBestNameCandidate(String ocrText, String expectedFullName) {
        if (ocrText == null || ocrText.isBlank()) {
            return "";
        }

        String normalizedExpected = normalizeWithSpaces(expectedFullName);
        String[] expectedTokens = Arrays.stream(normalizedExpected.split(" "))
                .filter(token -> !token.isBlank())
                .toArray(String[]::new);

        String bestLine = "";
        int bestScore = -1;
        for (String rawLine : ocrText.split("\\R")) {
            String line = rawLine.replaceAll("\\s+", " ").trim();
            if (line.isBlank()) {
                continue;
            }

            String normalizedLine = normalizeWithSpaces(line);
            if (normalizedLine.isBlank()) {
                continue;
            }

            int score = 0;
            String compactLine = normalize(line);
            for (String token : expectedTokens) {
                if (isTokenMatched(token, normalizedLine.split(" "), compactLine)) {
                    score++;
                }
            }

            if (score > bestScore || (score == bestScore && line.length() < bestLine.length())) {
                bestScore = score;
                bestLine = line;
            }
        }

        if (!bestLine.isBlank()) {
            return bestLine;
        }
        return abbreviate(ocrText);
    }

    private String limitLength(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }
}
