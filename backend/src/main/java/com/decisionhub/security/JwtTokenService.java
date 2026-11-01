package com.decisionhub.security;

import java.time.Instant;
import java.util.Date;
import java.util.List;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.decisionhub.user.AppUser;
import com.decisionhub.user.AccountRole;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtTokenService {
	private final SecretKey signingKey;
	private final long expirationMinutes;

	public JwtTokenService(
			@Value("${app.jwt.secret}") String encodedSecret,
			@Value("${app.jwt.expiration-minutes}") long expirationMinutes) {
		if (encodedSecret == null || encodedSecret.isBlank()) {
			throw new IllegalStateException("JWT_SECRET must be set to a base64-encoded key of at least 32 bytes.");
		}
		this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(encodedSecret));
		this.expirationMinutes = expirationMinutes;
	}

	public String issue(AppUser user) {
		Instant now = Instant.now();
		Instant expiresAt = now.plusSeconds(expirationMinutes * 60);
		AccountRole role = user.getRole();

		return Jwts.builder()
				.subject(user.getEmail())
				.claim("roles", List.of(role.name()))
				.issuedAt(Date.from(now))
				.expiration(Date.from(expiresAt))
				.signWith(signingKey)
				.compact();
	}

	public Claims parse(String token) {
		return Jwts.parser()
				.verifyWith(signingKey)
				.build()
				.parseSignedClaims(token)
				.getPayload();
	}

	public long getExpirationSeconds() {
		return expirationMinutes * 60;
	}
}
