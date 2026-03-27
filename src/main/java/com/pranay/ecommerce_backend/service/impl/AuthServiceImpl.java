package com.pranay.ecommerce_backend.service.impl;

import com.pranay.ecommerce_backend.dto.auth.AuthResponse;
import com.pranay.ecommerce_backend.dto.auth.LoginRequest;
import com.pranay.ecommerce_backend.dto.auth.RegisterRequest;
import com.pranay.ecommerce_backend.entity.Cart;
import com.pranay.ecommerce_backend.entity.Role;
import com.pranay.ecommerce_backend.entity.User;
import com.pranay.ecommerce_backend.exception.ValidationException;
import com.pranay.ecommerce_backend.repository.CartRepository;
import com.pranay.ecommerce_backend.repository.UserRepository;
import com.pranay.ecommerce_backend.security.CustomUserDetailsService;
import com.pranay.ecommerce_backend.security.JwtService;
import com.pranay.ecommerce_backend.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final JwtService jwtService;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.debug("Registering new user with email: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            log.debug("Registration failed: Email already exists - {}", request.getEmail());
            throw new ValidationException("Email already registered");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .build();
        User savedUser = userRepository.save(user);
        log.debug("User saved with id: {}", savedUser.getId());

        Cart cart = Cart.builder().user(savedUser).build();
        cartRepository.save(cart);
        log.debug("Cart created for user id: {}", savedUser.getId());

        UserDetails userDetails = userDetailsService.loadUserByUsername(savedUser.getEmail());
        String token = jwtService.generateToken(userDetails);
        log.debug("JWT token generated for user id: {}", savedUser.getId());

        return buildAuthResponse(savedUser, token);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        log.debug("Login attempt for email: {}", request.getEmail());

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        log.debug("Authentication successful for email: {}", request.getEmail());

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.debug("Login failed: User not found - {}", request.getEmail());
                    return new ValidationException("User not found");
                });

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String token = jwtService.generateToken(userDetails);
        log.debug("JWT token generated for login, user id: {}", user.getId());

        return buildAuthResponse(user, token);
    }

    private AuthResponse buildAuthResponse(User user, String token) {
        return AuthResponse.builder()
                .token(token)
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }
}