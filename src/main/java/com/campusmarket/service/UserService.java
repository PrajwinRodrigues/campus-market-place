package com.campusmarket.service;

import com.campusmarket.dto.user.UpdateProfileRequest;
import com.campusmarket.dto.user.UserResponse;
import com.campusmarket.entity.User;
import com.campusmarket.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserResponse getProfile(User currentUser) {
        return UserResponse.from(currentUser);
    }

    @Transactional
    public UserResponse updateProfile(User currentUser, UpdateProfileRequest request) {
        currentUser.setFullName(request.fullName());
        currentUser.setPhone(request.phone());
        currentUser.setCollegeName(request.collegeName());
        currentUser.setHostelOrLocation(request.hostelOrLocation());
        User saved = userRepository.save(currentUser);
        return UserResponse.from(saved);
    }
}
