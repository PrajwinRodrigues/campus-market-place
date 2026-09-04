package com.campusmarket.controller;

import com.campusmarket.dto.favorite.FavoriteResponse;
import com.campusmarket.entity.User;
import com.campusmarket.service.FavoriteService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
@Tag(name = "Favorites")
@SecurityRequirement(name = "bearerAuth")
public class FavoriteController {

    private final FavoriteService favoriteService;

    @PostMapping("/{listingId}")
    public ResponseEntity<FavoriteResponse> add(@PathVariable Long listingId,
                                                  @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED).body(favoriteService.addFavorite(listingId, currentUser));
    }

    @DeleteMapping("/{listingId}")
    public ResponseEntity<Void> remove(@PathVariable Long listingId, @AuthenticationPrincipal User currentUser) {
        favoriteService.removeFavorite(listingId, currentUser);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public List<FavoriteResponse> myFavorites(@AuthenticationPrincipal User currentUser) {
        return favoriteService.getMyFavorites(currentUser);
    }
}
