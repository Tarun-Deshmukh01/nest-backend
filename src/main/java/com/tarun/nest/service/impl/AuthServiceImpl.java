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

        long totalStart = System.currentTimeMillis();

        long emailCheckStart = System.currentTimeMillis();

        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email already registered");
        }

        log.info("Email existence check took: {} ms",
                System.currentTimeMillis() - emailCheckStart);

        if (!request.password().equals(request.confirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        long bcryptStart = System.currentTimeMillis();

        String encodedPassword = passwordEncoder.encode(request.password());

        log.info("BCrypt encoding took: {} ms",
                System.currentTimeMillis() - bcryptStart);

        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email());
        user.setMobileNumber(request.mobileNumber());
        user.setPassword(encodedPassword);
        user.setRole(Role.valueOf(request.role().toUpperCase()));
        user.setActive(true);

        long saveStart = System.currentTimeMillis();

        User savedUser = userRepository.save(user);

        log.info("Database save took: {} ms",
                System.currentTimeMillis() - saveStart);

        log.info("TOTAL registration took: {} ms",
                System.currentTimeMillis() - totalStart);

        return new RegisterResponse(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getRole().toString()
        );
    }

    @Override
    public LoginResponse login(LoginRequest request) {

        long totalStart = System.currentTimeMillis();

        long dbStart = System.currentTimeMillis();

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() ->
                        new AuthenticationException("Invalid email or password"));

        log.info("DB lookup took: {} ms",
                System.currentTimeMillis() - dbStart);

        if (!user.getActive()) {
            throw new AuthenticationException("User account is inactive");
        }

        long passwordStart = System.currentTimeMillis();

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new AuthenticationException("Invalid email or password");
        }

        log.info("BCrypt password check took: {} ms",
                System.currentTimeMillis() - passwordStart);

        long jwtStart = System.currentTimeMillis();

        String token = jwtUtil.generateToken(
                user.getId(),
                user.getEmail(),
                user.getRole().toString()
        );

        log.info("JWT generation took: {} ms",
                System.currentTimeMillis() - jwtStart);

        log.info("TOTAL login took: {} ms",
                System.currentTimeMillis() - totalStart);

        return new LoginResponse(
                token,
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getRole().toString()
        );
    }
}
