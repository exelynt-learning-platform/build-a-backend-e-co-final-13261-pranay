package com.pranay.ecommerce_backend.service;

import com.pranay.ecommerce_backend.dto.auth.AuthResponse;
import com.pranay.ecommerce_backend.dto.auth.LoginRequest;
import com.pranay.ecommerce_backend.dto.auth.RegisterRequest;
import com.pranay.ecommerce_backend.entity.Role;
import com.pranay.ecommerce_backend.repository.CartRepository;
import com.pranay.ecommerce_backend.repository.UserRepository;
import com.pranay.ecommerce_backend.security.CustomUserDetailsService;
import com.pranay.ecommerce_backend.security.JwtService;
import com.pranay.ecommerce_backend.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthServiceImpl authService;

    @BeforeEach
    void setup() {
        // ✅ Inject property manually
        ReflectionTestUtils.setField(authService, "defaultRole", "USER");
    }

    @Test
    void registerShouldCreateUserAndCart() {
        RegisterRequest request = new RegisterRequest();
        request.setName("Kunal");
        request.setEmail("kunal@example.com");
        request.setPassword("secret");

        var savedUser = com.pranay.ecommerce_backend.entity.User.builder()
                .id(1L)
                .name("Kunal")
                .email("kunal@example.com")
                .password("encoded")
                .role(Role.USER)
                .build();

        var springUser = new org.springframework.security.core.userdetails.User(
                "kunal@example.com", "encoded", java.util.List.of());

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encoded");
        when(userRepository.save(any())).thenReturn(savedUser);
        when(userDetailsService.loadUserByUsername(request.getEmail())).thenReturn(springUser);
        when(jwtService.generateToken(springUser)).thenReturn("jwt-token");

        AuthResponse response = authService.register(request);

        assertEquals("jwt-token", response.getToken());
        assertEquals(Role.USER, response.getRole());
        verify(cartRepository).save(any());
    }

    @Test
    void loginShouldAuthenticateAndReturnToken() {
        LoginRequest request = new LoginRequest();
        request.setEmail("kunal@example.com");
        request.setPassword("secret");

        var savedUser = com.pranay.ecommerce_backend.entity.User.builder()
                .id(1L)
                .name("Kunal")
                .email("kunal@example.com")
                .password("encoded")
                .role(Role.USER)
                .build();

        var springUser = new org.springframework.security.core.userdetails.User(
                "kunal@example.com", "encoded", java.util.List.of());

        when(userRepository.findByEmail(request.getEmail()))
                .thenReturn(java.util.Optional.of(savedUser));
        when(userDetailsService.loadUserByUsername(request.getEmail()))
                .thenReturn(springUser);
        when(jwtService.generateToken(springUser)).thenReturn("jwt-token");

        AuthResponse response = authService.login(request);

        verify(authenticationManager).authenticate(any());
        assertEquals("jwt-token", response.getToken());
    }
}