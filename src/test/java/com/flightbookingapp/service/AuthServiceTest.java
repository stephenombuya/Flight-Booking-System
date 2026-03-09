package com.flightbookingapp.service;

import com.flightbookingapp.TestDataFactory;
import com.flightbookingapp.dto.request.LoginRequest;
import com.flightbookingapp.dto.request.RegisterRequest;
import com.flightbookingapp.dto.response.AuthResponse;
import com.flightbookingapp.exception.ConflictException;
import com.flightbookingapp.model.Role;
import com.flightbookingapp.model.User;
import com.flightbookingapp.repository.UserRepository;
import com.flightbookingapp.security.JwtService;
import com.flightbookingapp.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link AuthServiceImpl} using Mockito (no Spring context).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService")
class AuthServiceTest {

    @Mock private UserRepository        userRepository;
    @Mock private PasswordEncoder       passwordEncoder;
    @Mock private JwtService            jwtService;
    @Mock private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthServiceImpl authService;

    @Nested
    @DisplayName("register()")
    class Register {

        @Test
        @DisplayName("should register new user and return JWT")
        void shouldRegisterNewUser() {
            RegisterRequest request = TestDataFactory.buildRegisterRequest();
            User savedUser = TestDataFactory.buildCustomer();

            when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
            when(passwordEncoder.encode(request.getPassword())).thenReturn("hashed");
            when(userRepository.save(any(User.class))).thenReturn(savedUser);
            when(jwtService.generateToken(any(User.class))).thenReturn("token.jwt");
            when(jwtService.getExpirationMs()).thenReturn(86400000L);

            AuthResponse response = authService.register(request);

            assertThat(response.getAccessToken()).isEqualTo("token.jwt");
            assertThat(response.getTokenType()).isEqualTo("Bearer");
            assertThat(response.getUser().getEmail()).isEqualTo(savedUser.getEmail());
            assertThat(response.getUser().getRole()).isEqualTo(Role.CUSTOMER);

            verify(userRepository).save(argThat(u ->
                    u.getEmail().equals(request.getEmail()) &&
                    u.getRole() == Role.CUSTOMER));
        }

        @Test
        @DisplayName("should throw ConflictException when email already exists")
        void shouldThrowWhenEmailExists() {
            RegisterRequest request = TestDataFactory.buildRegisterRequest();
            when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

            assertThatThrownBy(() -> authService.register(request))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining(request.getEmail());

            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("login()")
    class Login {

        @Test
        @DisplayName("should return JWT for valid credentials")
        void shouldLoginWithValidCredentials() {
            LoginRequest request = TestDataFactory.buildLoginRequest();
            User user = TestDataFactory.buildCustomer();

            when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
            when(jwtService.generateToken(user)).thenReturn("token.jwt");
            when(jwtService.getExpirationMs()).thenReturn(86400000L);

            AuthResponse response = authService.login(request);

            assertThat(response.getAccessToken()).isEqualTo("token.jwt");
            verify(authenticationManager).authenticate(any());
        }
    }
}
