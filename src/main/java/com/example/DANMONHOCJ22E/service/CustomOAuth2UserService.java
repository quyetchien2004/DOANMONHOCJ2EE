package com.example.DANMONHOCJ22E.service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.example.DANMONHOCJ22E.model.User;
import com.example.DANMONHOCJ22E.repository.UserRepository;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final UserVoucherService userVoucherService;

    public CustomOAuth2UserService(UserRepository userRepository,
                                   UserVoucherService userVoucherService) {
        this.userRepository = userRepository;
        this.userVoucherService = userVoucherService;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String googleId = oAuth2User.getName();
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String picture = oAuth2User.getAttribute("picture");

        // Tìm user theo googleId hoặc email
        Optional<User> existingUser = userRepository.findByGoogleId(googleId);
        
        User user;
        boolean isNewUser = false;
        
        if (existingUser.isPresent()) {
            user = existingUser.get();
            // Cập nhật thông tin từ Google nếu cần
            user.setProfileImageUrl(picture);
        } else {
            // Kiểm tra xem email đã tồn tại chưa (từ đăng ký bình thường)
            User userByEmail = userRepository.findByEmail(email);
            
            if (userByEmail != null) {
                user = userByEmail;
                user.setGoogleId(googleId);
                user.setOauthProvider("google");
                user.setProfileImageUrl(picture);
            } else {
                // Tạo user mới
                user = new User();
                user.setGoogleId(googleId);
                user.setEmail(email);
                user.setFullName(name);
                user.setUsername(generateUsername(email));
                user.setPassword(""); // OAuth user không có password
                user.setOauthProvider("google");
                user.setProfileImageUrl(picture);
                user.setRole("ROLE_USER");
                isNewUser = true;
            }
        }

        userRepository.save(user);
        
        // Auto-assign WELCOME10 voucher nếu user mới
        if (isNewUser) {
            userVoucherService.issueIfMissing(user,
                    UserVoucherService.REASON_NEW_ACCOUNT_10,
                    UserVoucherService.CODE_WELCOME10,
                    BigDecimal.TEN,
                    45);
        }
        
        // Return OAuth2User với username làm name attribute
        Map<String, Object> attributes = new HashMap<>(oAuth2User.getAttributes());
        return new DefaultOAuth2User(
            oAuth2User.getAuthorities(),
            attributes,
            "sub"
        ) {
            @Override
            public String getName() {
                // Trả về username thay vì googleId
                return user.getUsername();
            }
        };
    }

    /**
     * Tạo username từ email (lấy phần trước @)
     */
    private String generateUsername(String email) {
        String username = email.split("@")[0];
        
        // Kiểm tra username đã tồn tại chưa
        int counter = 1;
        String originalUsername = username;
        User existingUser = userRepository.findByUsername(username);
        while (existingUser != null) {
            username = originalUsername + counter;
            counter++;
            existingUser = userRepository.findByUsername(username);
        }
        
        return username;
    }
}
