package com.campusmarket.controller;

import com.campusmarket.dto.user.UpdateProfileRequest;
import com.campusmarket.dto.user.UserResponse;
import com.campusmarket.entity.User;
import com.campusmarket.service.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal User currentUser) {
        return userService.getProfile(currentUser);
    }

    @PutMapping("/me")
    public UserResponse updateMe(@AuthenticationPrincipal User currentUser,
                                  @Valid @RequestBody UpdateProfileRequest request) {
        return userService.updateProfile(currentUser, request);
    }
}
