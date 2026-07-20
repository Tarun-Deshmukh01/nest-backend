package com.tarun.nest.service;

import com.tarun.nest.dto.LoginRequest;
import com.tarun.nest.dto.LoginResponse;
import com.tarun.nest.dto.RegisterRequest;
import com.tarun.nest.dto.RegisterResponse;
import com.tarun.nest.entity.User;
import com.tarun.nest.repository.UserRepository;
import com.tarun.nest.util.JwtUtil;
import com.tarun.nest.enums.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    private static final String SECURITY_CODE = "1234";
    
    public LoginResponse login(LoginRequest loginRequest) {
        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found with email: " + loginRequest.getEmail()));
        
        if (!user.getActive()) {
            throw new RuntimeException("User account is inactive");
        }
        
        if (!loginRequest.getPassword().equals(user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }
        
        String token = jwtUtil.generateToken(user.getEmail());
        
        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setMessage("Login successful");
        response.setUserId(user.getId());
        response.setEmail(user.getEmail());
        response.setName(user.getName());
        
        return response;
    }
    
    public RegisterResponse register(RegisterRequest registerRequest) {
        // Validate passwords match
        if (!registerRequest.getPassword().equals(registerRequest.getConfirmPassword())) {
            throw new RuntimeException("Passwords do not match");
        }
        
        // Validate security code
        if (!registerRequest.getSecurityCode().equals(SECURITY_CODE)) {
            throw new RuntimeException("Invalid security code. Security code should be 1234");
        }
        
        // Check if email exists
        if (userRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }
        
        // Generate username from email (or use a UUID for uniqueness)
        String username = registerRequest.getEmail().split("@")[0] + "_" + System.currentTimeMillis();
        
        // Check if username exists (though we're generating it to be unique)
        if (userRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("Username already exists");
        }
        
        // Create new user
        User user = new User();
        user.setUsername(username);
        user.setEmail(registerRequest.getEmail());
        user.setPassword(registerRequest.getPassword());
        user.setName(registerRequest.getName());
        user.setSecurityCode(registerRequest.getSecurityCode());
        user.setRole(registerRequest.getRole() != null ? registerRequest.getRole() : UserRole.CUSTOMER);
        user.setActive(true);
        
        User savedUser = userRepository.save(user);
        
        RegisterResponse response = new RegisterResponse();
        response.setMessage("Registration successful");
        response.setSuccess(true);
        response.setUserId(savedUser.getId());
        response.setName(savedUser.getName());
        response.setEmail(savedUser.getEmail());
        response.setRole(savedUser.getRole());
        
        return response;
    }
}
