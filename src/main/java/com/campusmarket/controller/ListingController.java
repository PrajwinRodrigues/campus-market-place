package com.campusmarket.controller;

import com.campusmarket.dto.listing.ListingRequest;
import com.campusmarket.dto.listing.ListingResponse;
import com.campusmarket.dto.listing.ListingStatusUpdateRequest;
import com.campusmarket.entity.Category;
import com.campusmarket.entity.ItemCondition;
import com.campusmarket.entity.ListingStatus;
import com.campusmarket.entity.User;
import com.campusmarket.service.FileStorageService;
import com.campusmarket.service.ListingService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/listings")
@RequiredArgsConstructor
@Tag(name = "Listings")
public class ListingController {

    private final ListingService listingService;
    private final FileStorageService fileStorageService;

    @PostMapping
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ListingResponse> create(@Valid @RequestBody ListingRequest request,
                                                    @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED).body(listingService.create(request, currentUser));
    }

    @GetMapping
    public Page<ListingResponse> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Category category,
            @RequestParam(required = false) ItemCondition condition,
            @RequestParam(required = false) ListingStatus status,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return listingService.search(keyword, category, condition, status, minPrice, maxPrice, pageable);
    }

    @GetMapping("/{id}")
    public ListingResponse getById(@PathVariable Long id) {
        return listingService.getById(id);
    }

    @GetMapping("/mine")
    @SecurityRequirement(name = "bearerAuth")
    public Page<ListingResponse> myListings(@AuthenticationPrincipal User currentUser,
                                             @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return listingService.getMyListings(currentUser, pageable);
    }

    @PutMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    public ListingResponse update(@PathVariable Long id,
                                   @Valid @RequestBody ListingRequest request,
                                   @AuthenticationPrincipal User currentUser) {
        return listingService.update(id, request, currentUser);
    }

    @PatchMapping("/{id}/status")
    @SecurityRequirement(name = "bearerAuth")
    public ListingResponse updateStatus(@PathVariable Long id,
                                         @Valid @RequestBody ListingStatusUpdateRequest request,
                                         @AuthenticationPrincipal User currentUser) {
        return listingService.updateStatus(id, request.status(), currentUser);
    }

    @PostMapping("/{id}/images")
    @SecurityRequirement(name = "bearerAuth")
    public Map<String, String> uploadImage(@PathVariable Long id,
                                            @RequestParam("file") MultipartFile file,
                                            @AuthenticationPrincipal User currentUser) {
        // Reject unauthorized uploads before writing an orphan file to disk.
        listingService.assertCanModify(id, currentUser);
        String storedUrl = fileStorageService.store(file);
        listingService.addImage(id, storedUrl, currentUser);
        return Map.of("imageUrl", storedUrl);
    }

    @DeleteMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> delete(@PathVariable Long id, @AuthenticationPrincipal User currentUser) {
        listingService.delete(id, currentUser);
        return ResponseEntity.noContent().build();
    }
}
