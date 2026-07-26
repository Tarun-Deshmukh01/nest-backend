package com.tarun.nest.service.impl;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.tarun.nest.dto.LoginRequest;
import com.tarun.nest.dto.LoginResponse;
import com.tarun.nest.dto.RegisterRequest;
import com.tarun.nest.dto.RegisterResponse;
import com.tarun.nest.entity.User;
import com.tarun.nest.enums.UserRole;
import com.tarun.nest.exception.EmailAlreadyExistsException;
import com.tarun.nest.exception.MobileAlreadyExistsException;
import com.tarun.nest.exception.PasswordMismatchException;
import com.tarun.nest.repository.UserRepository;
import com.tarun.nest.security.JwtUtil;
import com.tarun.nest.service.AuthService;

@Service
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           JwtUtil jwtUtil) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public RegisterResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Email already registered.");
        }

        if (userRepository.existsByMobileNumber(request.getMobileNumber())) {
            throw new MobileAlreadyExistsException("Mobile number already registered.");
        }

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new PasswordMismatchException("Password and Confirm Password do not match.");
        }

        if (request.getRole() == null) {
            throw new IllegalArgumentException("Role is required.");
        }

        if (request.getRole() != UserRole.CUSTOMER &&
            request.getRole() != UserRole.VENDOR) {

            throw new IllegalArgumentException("Invalid role.");
        }

        User user = new User();

        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setMobileNumber(request.getMobileNumber());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
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

    @Override
    public LoginResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new RuntimeException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }

        String token = jwtUtil.generateToken(user.getEmail());

        LoginResponse response = new LoginResponse();

        response.setToken(token);
        response.setMessage("Login Successful");
        response.setUserId(user.getId());
        response.setEmail(user.getEmail());
        response.setName(user.getName());

        return response;
    }
}