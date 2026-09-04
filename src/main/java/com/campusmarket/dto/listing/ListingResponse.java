package com.campusmarket.dto.listing;

import com.campusmarket.entity.Listing;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record ListingResponse(
        Long id,
        String title,
        String description,
        BigDecimal price,
        String category,
        String condition,
        String status,
        String location,
        List<String> imageUrls,
        Long sellerId,
        String sellerName,
        String sellerPhone,
        Instant createdAt,
        Instant updatedAt
) {
    public static ListingResponse from(Listing listing) {
        return new ListingResponse(
                listing.getId(),
                listing.getTitle(),
                listing.getDescription(),
                listing.getPrice(),
                listing.getCategory().name(),
                listing.getCondition().name(),
                listing.getStatus().name(),
                listing.getLocation(),
                // Copy while the session is still open (inside a @Transactional
                // method) — the lazy collection itself can't survive past that
                // to Jackson serialization.
                List.copyOf(listing.getImageUrls()),
                listing.getSeller().getId(),
                listing.getSeller().getFullName(),
                listing.getSeller().getPhone(),
                listing.getCreatedAt(),
                listing.getUpdatedAt()
        );
    }
}
