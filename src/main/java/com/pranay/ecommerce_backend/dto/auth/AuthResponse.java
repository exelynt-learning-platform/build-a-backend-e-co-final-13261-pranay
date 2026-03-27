package com.pranay.ecommerce_backend.dto.auth;

import com.pranay.ecommerce_backend.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class AuthResponse {

    private final String token;
    private final Long userId;
    private final String name;
    private final String email;
    private final Role role;
}
