package com.tarun.nest.service;

import com.tarun.nest.dto.RegisterRequest;
import com.tarun.nest.dto.RegisterResponse;

public interface AuthService {

    RegisterResponse register(RegisterRequest request);
}