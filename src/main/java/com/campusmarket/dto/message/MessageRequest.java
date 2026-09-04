package com.campusmarket.dto.message;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MessageRequest(
        @NotNull Long listingId,
        @NotNull Long receiverId,
        @NotBlank @Size(max = 2000) String content
) {
}
