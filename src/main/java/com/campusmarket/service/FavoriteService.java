package com.campusmarket.service;

import com.campusmarket.dto.favorite.FavoriteResponse;
import com.campusmarket.entity.Favorite;
import com.campusmarket.entity.Listing;
import com.campusmarket.entity.User;
import com.campusmarket.exception.BadRequestException;
import com.campusmarket.exception.ResourceNotFoundException;
import com.campusmarket.repository.FavoriteRepository;
import com.campusmarket.repository.ListingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final ListingRepository listingRepository;

    @Transactional
    public FavoriteResponse addFavorite(Long listingId, User currentUser) {
        if (favoriteRepository.existsByUserIdAndListingId(currentUser.getId(), listingId)) {
            throw new BadRequestException("Listing is already in your favorites");
        }
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new ResourceNotFoundException("Listing not found: " + listingId));

        Favorite favorite = Favorite.builder()
                .user(currentUser)
                .listing(listing)
                .build();

        return FavoriteResponse.from(favoriteRepository.save(favorite));
    }

    @Transactional
    public void removeFavorite(Long listingId, User currentUser) {
        favoriteRepository.deleteByUserIdAndListingId(currentUser.getId(), listingId);
    }

    @Transactional(readOnly = true)
    public List<FavoriteResponse> getMyFavorites(User currentUser) {
        return favoriteRepository.findByUserIdOrderByCreatedAtDesc(currentUser.getId())
                .stream()
                .map(FavoriteResponse::from)
                .toList();
    }
}
