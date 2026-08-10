package com.tarun.nest.service.impl;

import com.tarun.nest.dto.LoginRequest;
import com.tarun.nest.dto.LoginResponse;
import com.tarun.nest.dto.RegisterRequest;
import com.tarun.nest.dto.RegisterResponse;
import com.tarun.nest.entity.Role;
import com.tarun.nest.entity.User;
import com.tarun.nest.exception.AuthenticationException;
import com.tarun.nest.repository.UserRepository;
import com.tarun.nest.service.AuthService;
import com.tarun.nest.util.JwtUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public RegisterResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.email())) {
            log.warn("Registration attempt with existing email: {}", request.email());
            throw new IllegalArgumentException("Email already registered");
        }

        if (!request.password().equals(request.confirmPassword())) {
            log.warn("Registration attempt with mismatched passwords for: {}", request.email());
            throw new IllegalArgumentException("Passwords do not match");
        }

        if (request.role() == Role.ADMIN) {
            log.warn("Registration attempt with admin role for: {}", request.email());
            throw new IllegalArgumentException("Admin registration is not allowed");
        }

        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPhoneNumber(request.phoneNumber());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(request.role());
        user.setActive(true);

        User savedUser = userRepository.save(user);
        log.info("User registered successfully: {}", savedUser.getEmail());

        return new RegisterResponse(savedUser.getId(), savedUser.getEmail());
    }

    @Override
    public LoginResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> {
                    log.warn("Login attempt with non-existent email: {}", request.email());
                    return new AuthenticationException("Invalid email or password");
                });

        if (!user.getActive()) {
            log.warn("Login attempt for inactive user: {}", request.email());
            throw new AuthenticationException("User account is inactive");
        }

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            log.warn("Login attempt with wrong password for user: {}", request.email());
            throw new AuthenticationException("Invalid email or password");
        }

        String token = jwtUtil.generateToken(user.getEmail());
        log.info("User logged in successfully: {}", request.email());

        return new LoginResponse(token, user.getEmail());
    }
}