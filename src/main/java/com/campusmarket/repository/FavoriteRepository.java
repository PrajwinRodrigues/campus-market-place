package com.campusmarket.repository;

import com.campusmarket.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    Optional<Favorite> findByUserIdAndListingId(Long userId, Long listingId);

    boolean existsByUserIdAndListingId(Long userId, Long listingId);

    List<Favorite> findByUserIdOrderByCreatedAtDesc(Long userId);

    void deleteByUserIdAndListingId(Long userId, Long listingId);
}
