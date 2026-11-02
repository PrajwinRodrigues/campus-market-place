package com.campusmarket.config;

import com.campusmarket.entity.*;
import com.campusmarket.repository.FavoriteRepository;
import com.campusmarket.repository.ListingRepository;
import com.campusmarket.repository.MessageRepository;
import com.campusmarket.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.List;

@Configuration
@Profile("local")
@RequiredArgsConstructor
public class LocalDemoData {

    @Bean
    CommandLineRunner seedLocalMarketplace(UserRepository users,
                                           ListingRepository listings,
                                           FavoriteRepository favorites,
                                           MessageRepository messages,
                                           PasswordEncoder encoder) {
        return args -> {
            if (listings.count() > 0) return;

            User demo = users.save(User.builder()
                    .fullName("Aarav Mehta")
                    .email("demo@campus.test")
                    .password(encoder.encode("Campus123!"))
                    .phone("9876543210")
                    .collegeName("North Campus University")
                    .hostelOrLocation("Maple Residence")
                    .role(Role.STUDENT)
                    .build());

            User seller = users.save(User.builder()
                    .fullName("Mira Kapoor")
                    .email("mira@campus.test")
                    .password(encoder.encode("Campus123!"))
                    .phone("9876501234")
                    .collegeName("North Campus University")
                    .hostelOrLocation("Cedar Hall")
                    .role(Role.STUDENT)
                    .build());

            List<Listing> samples = List.of(
                    listing("Engineering Mathematics, 8th edition", "Clean copy with a few pencil notes in the first chapter. Great for second-year engineering.", "420", Category.BOOKS, ItemCondition.GOOD, "Library steps", seller),
                    listing("Wireless headphones", "Comfortable over-ear headphones. Battery still lasts all week; includes the charging cable.", "1250", Category.ELECTRONICS, ItemCondition.LIKE_NEW, "Student centre", seller),
                    listing("Desk lamp with warm light", "Compact lamp with adjustable neck. Perfect for a hostel desk and easy to carry.", "350", Category.HOSTEL_ESSENTIALS, ItemCondition.GOOD, "Maple Residence lobby", seller),
                    listing("Badminton racket", "Lightweight racket, recently restrung. Selling because I am moving out this semester.", "600", Category.SPORTS, ItemCondition.FAIR, "Sports complex", seller),
                    listing("Mini bookshelf", "Two-shelf wooden stand, sturdy and in great shape. Fits beside a single bed.", "800", Category.FURNITURE, ItemCondition.LIKE_NEW, "Cedar Hall", demo),
                    listing("Intro to Psychology notes", "Organized lecture notes for the full semester. Printed and ready to pick up.", "180", Category.BOOKS, ItemCondition.NEW, "Arts block", seller)
            );
            List<Listing> saved = listings.saveAll(samples);
            saved.get(5).setStatus(ListingStatus.SOLD);
            listings.save(saved.get(5));

            favorites.save(Favorite.builder().user(demo).listing(saved.get(1)).build());
            favorites.save(Favorite.builder().user(demo).listing(saved.get(2)).build());
            messages.save(Message.builder().listing(saved.get(1)).sender(demo).receiver(seller)
                    .content("Hi Mira, are the headphones still available?").build());
            messages.save(Message.builder().listing(saved.get(1)).sender(seller).receiver(demo)
                    .content("Yes! I can meet at the student centre after class.").read(true).build());
        };
    }

    private static Listing listing(String title, String description, String price,
                                    Category category, ItemCondition condition,
                                    String location, User seller) {
        return Listing.builder()
                .title(title)
                .description(description)
                .price(new BigDecimal(price))
                .category(category)
                .condition(condition)
                .location(location)
                .seller(seller)
                .status(ListingStatus.AVAILABLE)
                .build();
    }
}
