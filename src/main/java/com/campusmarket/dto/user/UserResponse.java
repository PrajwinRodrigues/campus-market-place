package com.campusmarket.dto.user;

import com.campusmarket.entity.User;

public record UserResponse(
        Long id,
        String fullName,
        String email,
        String phone,
        String collegeName,
        String hostelOrLocation,
        String role
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getPhone(),
                user.getCollegeName(),
                user.getHostelOrLocation(),
                user.getRole().name()
        );
    }
}
