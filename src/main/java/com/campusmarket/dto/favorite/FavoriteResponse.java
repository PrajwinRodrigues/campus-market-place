package com.campusmarket.dto.favorite;

import com.campusmarket.dto.listing.ListingResponse;
import com.campusmarket.entity.Favorite;

import java.time.Instant;

public record FavoriteResponse(
        Long id,
        ListingResponse listing,
        Instant createdAt
) {
    public static FavoriteResponse from(Favorite favorite) {
        return new FavoriteResponse(
                favorite.getId(),
                ListingResponse.from(favorite.getListing()),
                favorite.getCreatedAt()
        );
    }
}
