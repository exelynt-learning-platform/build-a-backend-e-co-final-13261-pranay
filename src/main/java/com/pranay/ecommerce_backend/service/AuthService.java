package com.pranay.ecommerce_backend.service;

import com.pranay.ecommerce_backend.dto.auth.AuthResponse;
import com.pranay.ecommerce_backend.dto.auth.LoginRequest;
import com.pranay.ecommerce_backend.dto.auth.RegisterRequest;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);
}
