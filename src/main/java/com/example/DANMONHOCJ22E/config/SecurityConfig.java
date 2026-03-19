package com.example.DANMONHOCJ22E.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
public class SecurityConfig {

    // Mã hóa mật khẩu
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/", "/home",
                                "/index-2",
                                "/about", "/contact", "/faq",
                                "/blog", "/blog-details",
                                "/pricing", "/projects",
                                "/services", "/single-service",
                                "/single-rooms",
                                "/team", "/team-single",
                                "/shop", "/shop-details",
                                "/room", "/booking",
                                "/chatbot",
                                "/login", "/register", "/access-denied",
                                "/api/hotels/**",
                                "/api/chatbot/ask",
                                "/api/payments/vnpay/callback",
                                "/api/account/request-password-otp",
                                "/api/account/reset-password",
                                "/css/**", "/js/**", "/images/**", "/img/**", "/fonts/**", "/fontawesome/**", "/fontawesome-pro/**"
                        ).permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/users/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )

                .formLogin(form -> form
                        .loginPage("/login")              // Trang login tự tạo
                        .loginProcessingUrl("/login")    // URL xử lý login
                        .defaultSuccessUrl("/", true)    // Login thành công về trang chủ
                        .failureUrl("/login?error=true") // Sai mật khẩu quay lại login
                        .permitAll()
                )

                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )

                .exceptionHandling(exception -> exception
                        .defaultAuthenticationEntryPointFor(
                                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                                new AntPathRequestMatcher("/api/**")
                        )
                        .accessDeniedPage("/access-denied")
                );

        return http.build();
    }
}