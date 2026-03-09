package com.flightbookingapp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flightbookingapp.TestDataFactory;
import com.flightbookingapp.dto.request.LoginRequest;
import com.flightbookingapp.dto.request.RegisterRequest;
import com.flightbookingapp.dto.response.AuthResponse;
import com.flightbookingapp.dto.response.UserResponse;
import com.flightbookingapp.service.AuthService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@DisplayName("AuthController")
class AuthControllerTest {

    @Autowired MockMvc     mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean AuthService authService;
    // Required by SecurityConfig
    @MockBean com.flightbookingapp.security.JwtService jwtService;
    @MockBean org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    private AuthResponse mockAuthResponse;

    @BeforeEach
    void setUp() {
        UserResponse user = UserResponse.builder()
                .id(1L).email("jane.doe@example.com")
                .firstName("Jane").lastName("Doe")
                .build();
        mockAuthResponse = AuthResponse.builder()
                .accessToken("mock.jwt.token")
                .tokenType("Bearer")
                .expiresIn(86400L)
                .user(user)
                .build();
    }

    @Test
    @DisplayName("POST /api/v1/auth/register returns 201 with token")
    void register_returns201() throws Exception {
        when(authService.register(any(RegisterRequest.class))).thenReturn(mockAuthResponse);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(TestDataFactory.buildRegisterRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("mock.jwt.token"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/register returns 422 for invalid payload")
    void register_validationFails() throws Exception {
        RegisterRequest invalid = new RegisterRequest();
        // empty — all fields missing

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.fieldErrors").exists());
    }

    @Test
    @DisplayName("POST /api/v1/auth/login returns 200 with token")
    void login_returns200() throws Exception {
        when(authService.login(any(LoginRequest.class))).thenReturn(mockAuthResponse);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(TestDataFactory.buildLoginRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login returns 422 for blank email")
    void login_blankEmail() throws Exception {
        LoginRequest bad = new LoginRequest();
        bad.setEmail("");
        bad.setPassword("Password1");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bad)))
                .andExpect(status().isUnprocessableEntity());
    }
}
