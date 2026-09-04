package com.campusmarket.dto.listing;

import com.campusmarket.entity.ListingStatus;
import jakarta.validation.constraints.NotNull;

public record ListingStatusUpdateRequest(
        @NotNull ListingStatus status
) {
}
