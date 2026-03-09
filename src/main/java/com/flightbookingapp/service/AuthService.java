package com.flightbookingapp.service;

import com.flightbookingapp.dto.request.LoginRequest;
import com.flightbookingapp.dto.request.RegisterRequest;
import com.flightbookingapp.dto.response.AuthResponse;

/** Contract for authentication operations. */
public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
}
