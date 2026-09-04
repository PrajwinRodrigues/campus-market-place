package com.campusmarket.service;

import com.campusmarket.dto.auth.AuthResponse;
import com.campusmarket.dto.auth.LoginRequest;
import com.campusmarket.dto.auth.RegisterRequest;
import com.campusmarket.entity.Role;
import com.campusmarket.entity.User;
import com.campusmarket.exception.BadRequestException;
import com.campusmarket.repository.UserRepository;
import com.campusmarket.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BadRequestException("An account with this email already exists");
        }

        User user = User.builder()
                .fullName(request.fullName())
                .email(request.email().toLowerCase())
                .password(passwordEncoder.encode(request.password()))
                .phone(request.phone())
                .collegeName(request.collegeName())
                .hostelOrLocation(request.hostelOrLocation())
                .role(Role.STUDENT)
                .build();

        user = userRepository.save(user);

        String token = jwtService.generateToken(user);
        return AuthResponse.of(token, user.getId(), user.getFullName(), user.getEmail(), user.getRole().name());
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email().toLowerCase(), request.password()));

        User user = userRepository.findByEmail(request.email().toLowerCase())
                .orElseThrow(() -> new BadRequestException("Invalid email or password"));

        String token = jwtService.generateToken(user);
        return AuthResponse.of(token, user.getId(), user.getFullName(), user.getEmail(), user.getRole().name());
    }
}
