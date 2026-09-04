package com.campusmarket.dto.auth;

public record AuthResponse(
        String token,
        String tokenType,
        Long userId,
        String fullName,
        String email,
        String role
) {
    public static AuthResponse of(String token, Long userId, String fullName, String email, String role) {
        return new AuthResponse(token, "Bearer", userId, fullName, email, role);
    }
}
