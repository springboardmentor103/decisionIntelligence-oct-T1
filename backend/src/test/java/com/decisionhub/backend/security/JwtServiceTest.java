package com.decisionhub.backend.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    // Valid Base64-encoded 256-bit secret key for testing
    private static final String TEST_SECRET = "VGhpc0lzQVNlY3JldEtleUZvckRlY2lzaW9uSHViSldUU2lnbmluZzIwMjY=";
    private static final long ONE_HOUR_MS = 3600000L;

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(TEST_SECRET, ONE_HOUR_MS);
    }

    @Test
    @DisplayName("Should successfully generate a non-null, structured JWT token")
    void shouldGenerateValidToken() {
        String email = "alice@example.com";

        String token = jwtService.generateToken(email);

        assertThat(token).isNotNull().isNotBlank();
        assertThat(token.split("\\.")).hasSize(3); // header.payload.signature
    }

    @Test
    @DisplayName("Should parse a valid token and correctly extract the subject email")
    void shouldExtractCorrectEmailFromValidToken() {
        String email = "bob@example.com";
        String token = jwtService.generateToken(email);

        String extractedEmail = jwtService.extractEmail(token);

        assertThat(extractedEmail).isEqualTo(email);
    }

    @Test
    @DisplayName("Should confirm validity of an active unexpired token")
    void shouldValidateActiveToken() {
        String email = "carol@example.com";
        String token = jwtService.generateToken(email);

        boolean isValid = jwtService.isTokenValid(token);

        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("Should reject an expired JWT token")
    void shouldRejectExpiredToken() {
        // JwtService configured with negative expiration
        JwtService expiredJwtService = new JwtService(TEST_SECRET, -1000L);
        String expiredToken = expiredJwtService.generateToken("expired@example.com");

        boolean isValid = jwtService.isTokenValid(expiredToken);

        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should reject a malformed JWT token string")
    void shouldRejectMalformedToken() {
        assertThat(jwtService.isTokenValid("not-a-valid-jwt")).isFalse();
        assertThat(jwtService.isTokenValid("")).isFalse();
        assertThat(jwtService.isTokenValid("header.payload")).isFalse();
    }

    @Test
    @DisplayName("Should reject a token signed with a different secret key")
    void shouldRejectTokenWithInvalidSignature() {
        String differentSecret = "QW5vdGhlclNlY3JldEtleUZvclRlc3RpbmdTaWduaW5nSW52YWxpZDIwMjY=";
        SecretKey otherKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(differentSecret));

        String foreignToken = Jwts.builder()
                .subject("tampered@example.com")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 60000))
                .signWith(otherKey)
                .compact();

        boolean isValid = jwtService.isTokenValid(foreignToken);

        assertThat(isValid).isFalse();
    }
}

