package com.campusmarket.dto.listing;

import com.campusmarket.entity.Category;
import com.campusmarket.entity.ItemCondition;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public record ListingRequest(
        @NotBlank @Size(max = 150) String title,

        @NotBlank @Size(max = 4000) String description,

        @NotNull @DecimalMin(value = "0.0", inclusive = true, message = "Price cannot be negative")
        BigDecimal price,

        @NotNull Category category,

        @NotNull ItemCondition condition,

        String location,

        List<String> imageUrls
) {
}
