package com.tarun.nest.service;

import com.tarun.nest.dto.LoginRequest;
import com.tarun.nest.dto.LoginResponse;
import com.tarun.nest.dto.RegisterRequest;
import com.tarun.nest.dto.RegisterResponse;
import com.tarun.nest.dto.ApiResponse;

public interface AuthService {

    RegisterResponse register(RegisterRequest request);
    LoginResponse login(LoginRequest request);
//    ApiResponse logout(String token);
	ApiResponse logout();

}