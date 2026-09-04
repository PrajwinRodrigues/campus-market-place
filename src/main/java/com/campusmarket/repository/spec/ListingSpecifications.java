package com.campusmarket.repository.spec;

import com.campusmarket.entity.Category;
import com.campusmarket.entity.ItemCondition;
import com.campusmarket.entity.Listing;
import com.campusmarket.entity.ListingStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Builds a dynamic Specification for listing search/filter so we don't need
 * a hand-rolled query for every combination of optional filters.
 */
public final class ListingSpecifications {

    private ListingSpecifications() {
    }

    public static Specification<Listing> withFilters(String keyword,
                                                       Category category,
                                                       ItemCondition condition,
                                                       ListingStatus status,
                                                       BigDecimal minPrice,
                                                       BigDecimal maxPrice) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (keyword != null && !keyword.isBlank()) {
                // No need for LOWER() here (and Hibernate 6.4.4's `lower` function
                // descriptor has a Criteria type-validation bug that throws
                // "has type 'STRING', but argument is of type 'java.lang.String'"
                // regardless of how it's invoked): the database uses
                // utf8mb4_unicode_ci, which already makes LIKE case-insensitive.
                String like = "%" + keyword + "%";
                predicates.add(cb.or(
                        cb.like(root.get("title"), like),
                        cb.like(root.get("description"), like)
                ));
            }
            if (category != null) {
                predicates.add(cb.equal(root.get("category"), category));
            }
            if (condition != null) {
                predicates.add(cb.equal(root.get("condition"), condition));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (minPrice != null) {
                predicates.add(cb.ge(root.get("price"), minPrice));
            }
            if (maxPrice != null) {
                predicates.add(cb.le(root.get("price"), maxPrice));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
