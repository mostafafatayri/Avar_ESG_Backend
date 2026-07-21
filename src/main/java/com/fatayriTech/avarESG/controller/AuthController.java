package com.fatayriTech.avarESG.controller;

import com.fatayriTech.avarESG.dto.request.UserRequests.LoginRequest;
import com.fatayriTech.avarESG.dto.response.UserResponse.LoginResponse;
import com.fatayriTech.avarESG.service.AuthService.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Duration;

@RestController
@RequestMapping("${api.prefix}/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public LoginResponse login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response
    ) {
        LoginResponse loginResponse = authService.login(request);

        ResponseCookie refreshCookie =
                ResponseCookie.from("refreshToken", loginResponse.getRefreshToken())
                        .httpOnly(true)
                        .secure(false) // true in production with HTTPS
                        .sameSite("Lax")
                        .path("/")
                        .maxAge(Duration.ofDays(7))
                        .build();

        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        return LoginResponse.builder()
                .accessToken(loginResponse.getAccessToken())
                .user(loginResponse.getUser())
                .build();
    }

    @PostMapping("/refresh")
    public LoginResponse refresh(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        String refreshToken = extractRefreshTokenFromCookies(request);

        LoginResponse loginResponse = authService.refresh(refreshToken);

        ResponseCookie refreshCookie =
                ResponseCookie.from("refreshToken", loginResponse.getRefreshToken())
                        .httpOnly(true)
                        .secure(false) // true in production HTTPS
                        .sameSite("Lax")
                        .path("/")
                        .maxAge(Duration.ofDays(7))
                        .build();

        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        return LoginResponse.builder()
                .accessToken(loginResponse.getAccessToken())
                .user(loginResponse.getUser())
                .build();
    }

    @PostMapping("/logout")
    public void logout(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        String refreshToken = extractRefreshTokenFromCookies(request);

        authService.logout(refreshToken);

        ResponseCookie deleteCookie =
                ResponseCookie.from("refreshToken", "")
                        .httpOnly(true)
                        .secure(false)
                        .sameSite("Lax")
                        .path("/")
                        .maxAge(0)
                        .build();

        response.addHeader(HttpHeaders.SET_COOKIE, deleteCookie.toString());
    }

    private String extractRefreshTokenFromCookies(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return null;
        }

        for (Cookie cookie : request.getCookies()) {
            if ("refreshToken".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }

        return null;
    }
}