package com.campusmarket.service;

import com.campusmarket.dto.listing.ListingRequest;
import com.campusmarket.dto.listing.ListingResponse;
import com.campusmarket.entity.Category;
import com.campusmarket.entity.ItemCondition;
import com.campusmarket.entity.Listing;
import com.campusmarket.entity.ListingStatus;
import com.campusmarket.entity.Role;
import com.campusmarket.entity.User;
import com.campusmarket.exception.ForbiddenException;
import com.campusmarket.exception.ResourceNotFoundException;
import com.campusmarket.repository.ListingRepository;
import com.campusmarket.repository.spec.ListingSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class ListingService {

    private final ListingRepository listingRepository;

    @Transactional
    public ListingResponse create(ListingRequest request, User seller) {
        Listing listing = Listing.builder()
                .title(request.title())
                .description(request.description())
                .price(request.price())
                .category(request.category())
                .condition(request.condition())
                .location(request.location())
                .imageUrls(request.imageUrls() == null ? new ArrayList<>() : new ArrayList<>(request.imageUrls()))
                .status(ListingStatus.AVAILABLE)
                .seller(seller)
                .build();

        return ListingResponse.from(listingRepository.save(listing));
    }

    @Transactional(readOnly = true)
    public ListingResponse getById(Long id) {
        return ListingResponse.from(findOrThrow(id));
    }

    @Transactional(readOnly = true)
    public Page<ListingResponse> search(String keyword, Category category, ItemCondition condition,
                                         ListingStatus status, BigDecimal minPrice, BigDecimal maxPrice,
                                         Pageable pageable) {
        var spec = ListingSpecifications.withFilters(keyword, category, condition, status, minPrice, maxPrice);
        return listingRepository.findAll(spec, pageable).map(ListingResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<ListingResponse> getMyListings(User seller, Pageable pageable) {
        return listingRepository.findBySellerId(seller.getId(), pageable).map(ListingResponse::from);
    }

    @Transactional
    public ListingResponse update(Long id, ListingRequest request, User currentUser) {
        Listing listing = findOrThrow(id);
        requireOwnerOrAdmin(listing, currentUser);

        listing.setTitle(request.title());
        listing.setDescription(request.description());
        listing.setPrice(request.price());
        listing.setCategory(request.category());
        listing.setCondition(request.condition());
        listing.setLocation(request.location());
        if (request.imageUrls() != null) {
            listing.setImageUrls(new ArrayList<>(request.imageUrls()));
        }

        return ListingResponse.from(listingRepository.save(listing));
    }

    @Transactional
    public ListingResponse updateStatus(Long id, ListingStatus status, User currentUser) {
        Listing listing = findOrThrow(id);
        requireOwnerOrAdmin(listing, currentUser);
        listing.setStatus(status);
        return ListingResponse.from(listingRepository.save(listing));
    }

    @Transactional
    public String addImage(Long id, String imageUrl, User currentUser) {
        Listing listing = findOrThrow(id);
        requireOwnerOrAdmin(listing, currentUser);
        listing.getImageUrls().add(imageUrl);
        listingRepository.save(listing);
        return imageUrl;
    }

    @Transactional
    public void delete(Long id, User currentUser) {
        Listing listing = findOrThrow(id);
        requireOwnerOrAdmin(listing, currentUser);
        listingRepository.delete(listing);
    }

    private Listing findOrThrow(Long id) {
        return listingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Listing not found: " + id));
    }

    private void requireOwnerOrAdmin(Listing listing, User currentUser) {
        boolean isOwner = listing.getSeller().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;
        if (!isOwner && !isAdmin) {
            throw new ForbiddenException("You do not have permission to modify this listing");
        }
    }
}
