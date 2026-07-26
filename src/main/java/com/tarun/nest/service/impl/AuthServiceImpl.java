package com.tarun.nest.service.impl;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.tarun.nest.dto.RegisterRequest;
import com.tarun.nest.dto.RegisterResponse;
import com.tarun.nest.entity.User;
import com.tarun.nest.enums.UserRole;
import com.tarun.nest.repository.UserRepository;
import com.tarun.nest.service.AuthService;
import com.tarun.nest.exception.EmailAlreadyExistsException;
//import com.tarun.nest.exception.InvalidSecurityCodeException;
import com.tarun.nest.exception.MobileAlreadyExistsException;
import com.tarun.nest.exception.PasswordMismatchException;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public RegisterResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered.");
        }

        if (userRepository.existsByMobileNumber(request.getMobileNumber())) {
            throw new RuntimeException("Mobile number already registered.");
        }

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("Password and Confirm Password do not match.");
        }

        User user = new User();

        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setMobileNumber(request.getMobileNumber());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        if (request.getRole() == null) {
            throw new IllegalArgumentException("Role is required.");
        }

        if (request.getRole() != UserRole.CUSTOMER &&
            request.getRole() != UserRole.VENDOR) {

            throw new IllegalArgumentException("Invalid role.");
        }

        user.setRole(request.getRole());

        User savedUser = userRepository.save(user);

        return new RegisterResponse(
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getMobileNumber(),
                savedUser.getRole().name(),
                "Registration successful."
        );
    }

}