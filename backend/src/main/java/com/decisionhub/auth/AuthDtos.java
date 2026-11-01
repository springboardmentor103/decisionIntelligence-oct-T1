package com.decisionhub.auth;

import java.time.Instant;
import java.util.List;

import com.decisionhub.user.AccountRole;
import com.decisionhub.user.AppUser;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class AuthDtos {
	private AuthDtos() {
	}

	public record LoginRequest(
			@NotBlank @Email @Size(max = 320) String email,
			@NotBlank String password) {
	}

	public record RegisterRequest(
			@NotBlank @Size(min = 2, max = 120) String name,
			@NotBlank @Email @Size(max = 320) String email,
			@NotBlank @Size(min = 8, max = 72) String password) {
	}

	public record ForgotPasswordRequest(@NotBlank @Email @Size(max = 320) String email) {
	}

	public record ResetPasswordRequest(
			@NotBlank String token,
			@NotBlank @Size(min = 8, max = 72) String newPassword) {
	}

	public record UserResponse(
			Long id,
			String name,
			String email,
			AccountRole role,
			List<AccountRole> roles,
			Instant createdAt) {
		public static UserResponse from(AppUser user) {
			return new UserResponse(
					user.getId(),
					user.getName(),
					user.getEmail(),
					user.getRole(),
					List.of(user.getRole()),
					user.getCreatedAt());
		}
	}

	public record AuthResponse(String token, UserResponse user) {
	}

	public record MessageResponse(String message) {
	}

	public record CsrfTokenResponse(String token) {
	}

	public record ErrorResponse(String message) {
	}
}
