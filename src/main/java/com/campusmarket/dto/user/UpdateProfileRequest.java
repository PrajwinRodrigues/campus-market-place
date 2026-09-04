package com.campusmarket.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @NotBlank @Size(max = 120) String fullName,
        String phone,
        String collegeName,
        String hostelOrLocation
) {
}
