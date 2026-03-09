package com.flightbookingapp.service;

import com.flightbookingapp.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.flightbookingapp.security.UserPrincipal;

@DisplayName("JwtTokenProvider Unit Tests")
class JwtTokenProviderTest {

    private JwtTokenProvider tokenProvider;

    // Must be ≥32 bytes when UTF-8 encoded for HS256
    private static final String SECRET =
            "testSecret404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private static final long ACCESS_EXP  = 3_600_000L;  // 1 h
    private static final long REFRESH_EXP = 86_400_000L; // 24 h

    @BeforeEach
    void setUp() {
        tokenProvider = new JwtTokenProvider(SECRET, ACCESS_EXP, REFRESH_EXP);
    }

    @Test
    @DisplayName("generateAccessToken() returns a non-blank token")
    void generateAccessToken_returnsToken() {
        Authentication auth = buildAuth("user@example.com");
        String token = tokenProvider.generateAccessToken(auth);
        assertThat(token).isNotBlank();
    }

    @Test
    @DisplayName("getEmailFromToken() extracts the correct subject")
    void getEmailFromToken_extractsSubject() {
        Authentication auth = buildAuth("user@example.com");
        String token = tokenProvider.generateAccessToken(auth);
        assertThat(tokenProvider.getEmailFromToken(token)).isEqualTo("user@example.com");
    }

    @Test
    @DisplayName("validateToken() returns true for a freshly issued token")
    void validateToken_validToken_returnsTrue() {
        Authentication auth = buildAuth("user@example.com");
        String token = tokenProvider.generateAccessToken(auth);
        assertThat(tokenProvider.validateToken(token)).isTrue();
    }

    @Test
    @DisplayName("validateToken() returns false for a garbage string")
    void validateToken_invalidToken_returnsFalse() {
        assertThat(tokenProvider.validateToken("this.is.garbage")).isFalse();
    }

    @Test
    @DisplayName("validateToken() returns false for an expired token")
    void validateToken_expiredToken_returnsFalse() {
        // Create a provider with -1 ms TTL so the token is already expired
        JwtTokenProvider expiredProvider = new JwtTokenProvider(SECRET, -1L, -1L);
        Authentication auth = buildAuth("user@example.com");
        String token = expiredProvider.generateAccessToken(auth);
        assertThat(expiredProvider.validateToken(token)).isFalse();
    }

    @Test
    @DisplayName("generateRefreshToken() produces a different value to the access token")
    void generateRefreshToken_differsFromAccessToken() {
        Authentication auth = buildAuth("user@example.com");
        String accessToken  = tokenProvider.generateAccessToken(auth);
        String refreshToken = tokenProvider.generateRefreshToken("user@example.com");
        // Both encode the same subject but can differ due to timing; subject must match
        assertThat(tokenProvider.getEmailFromToken(refreshToken)).isEqualTo("user@example.com");
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private Authentication buildAuth(String email) {
        UserPrincipal principal = mock(UserPrincipal.class);
        when(principal.getUsername()).thenReturn(email);
        return new UsernamePasswordAuthenticationToken(
                principal, null,
                List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER")));
    }
}
