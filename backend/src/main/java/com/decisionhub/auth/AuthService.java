package com.decisionhub.auth;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.HexFormat;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import com.decisionhub.security.JwtTokenService;
import com.decisionhub.user.AccountRole;
import com.decisionhub.user.AppUser;
import com.decisionhub.user.AppUserRepository;

@Service
public class AuthService {
	private final AppUserRepository users;
	private final PasswordResetTokenRepository resetTokens;
	private final AuthenticationManager authenticationManager;
	private final PasswordEncoder passwordEncoder;
	private final JwtTokenService jwtTokenService;
	private final ObjectProvider<JavaMailSender> mailSenders;
	private final String frontendUrl;
	private final String mailFrom;
	private final long resetExpirationMinutes;
	private final SecureRandom secureRandom = new SecureRandom();

	public AuthService(
			AppUserRepository users,
			PasswordResetTokenRepository resetTokens,
			AuthenticationManager authenticationManager,
			PasswordEncoder passwordEncoder,
			JwtTokenService jwtTokenService,
			ObjectProvider<JavaMailSender> mailSenders,
			@Value("${app.frontend-url}") String frontendUrl,
			@Value("${app.mail.from:}") String mailFrom,
			@Value("${app.password-reset.expiration-minutes}") long resetExpirationMinutes) {
		this.users = users;
		this.resetTokens = resetTokens;
		this.authenticationManager = authenticationManager;
		this.passwordEncoder = passwordEncoder;
		this.jwtTokenService = jwtTokenService;
		this.mailSenders = mailSenders;
		this.frontendUrl = frontendUrl;
		this.mailFrom = mailFrom;
		this.resetExpirationMinutes = resetExpirationMinutes;
	}

	@Transactional
	public AuthDtos.AuthResponse register(AuthDtos.RegisterRequest request) {
		String email = normalizeEmail(request.email());
		if (users.existsByEmail(email)) {
			throw new ApiException(HttpStatus.CONFLICT, "An account with this email already exists.");
		}

		AppUser user = users.save(new AppUser(
				request.name().trim(),
				email,
				passwordEncoder.encode(request.password()),
				AccountRole.USER));
		return new AuthDtos.AuthResponse(jwtTokenService.issue(user), AuthDtos.UserResponse.from(user));
	}

	@Transactional(readOnly = true)
	public AuthDtos.AuthResponse login(AuthDtos.LoginRequest request) {
		String email = normalizeEmail(request.email());
		try {
			authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(email, request.password()));
		} catch (BadCredentialsException exception) {
			throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password.");
		}

		AppUser user = users.findByEmail(email)
				.orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password."));
		return new AuthDtos.AuthResponse(jwtTokenService.issue(user), AuthDtos.UserResponse.from(user));
	}

	@Transactional(readOnly = true)
	public AuthDtos.UserResponse currentUser(String email) {
		AppUser user = users.findByEmail(normalizeEmail(email))
				.orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Authenticated user no longer exists."));
		return AuthDtos.UserResponse.from(user);
	}

	@Transactional
	public AuthDtos.MessageResponse requestPasswordReset(String rawEmail) {
		JavaMailSender mailSender = mailSenders.getIfAvailable();
		if (mailSender == null || mailFrom.isBlank()) {
			throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "Password reset email is not configured.");
		}

		users.findByEmail(normalizeEmail(rawEmail)).ifPresent(user -> {
			resetTokens.deleteByUser(user);
			byte[] tokenBytes = new byte[32];
			secureRandom.nextBytes(tokenBytes);
			String rawToken = HexFormat.of().formatHex(tokenBytes);
			resetTokens.save(new PasswordResetToken(
					user,
					sha256(rawToken),
					Instant.now().plusSeconds(resetExpirationMinutes * 60)));

			String resetLink = UriComponentsBuilder.fromUriString(frontendUrl)
					.path("/reset-password")
					.queryParam("token", rawToken)
					.build()
					.toUriString();
			SimpleMailMessage message = new SimpleMailMessage();
			message.setFrom(mailFrom);
			message.setTo(user.getEmail());
			message.setSubject("Reset your DecisionHub password");
			message.setText("Use this link to reset your password. It expires in "
					+ resetExpirationMinutes + " minutes:\n\n" + resetLink);
			mailSender.send(message);
		});

		return new AuthDtos.MessageResponse(
				"If the address is registered, password reset instructions will be sent.");
	}

	@Transactional
	public void resetPassword(AuthDtos.ResetPasswordRequest request) {
		PasswordResetToken resetToken = resetTokens.findByTokenHash(sha256(request.token()))
				.orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Invalid or expired reset token."));
		if (!resetToken.getExpiresAt().isAfter(Instant.now())) {
			resetTokens.delete(resetToken);
			throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid or expired reset token.");
		}

		AppUser user = resetToken.getUser();
		user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
		users.save(user);
		resetTokens.deleteByUser(user);
	}

	private String normalizeEmail(String email) {
		return email.trim().toLowerCase();
	}

	private String sha256(String value) {
		try {
			return HexFormat.of().formatHex(
					MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
		} catch (NoSuchAlgorithmException exception) {
			throw new IllegalStateException("SHA-256 is required by the Java runtime.", exception);
		}
	}
}
