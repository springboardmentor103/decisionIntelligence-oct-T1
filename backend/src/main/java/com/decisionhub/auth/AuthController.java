package com.decisionhub.auth;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.decisionhub.security.JwtTokenService;
import com.decisionhub.security.OAuthLoginSuccessHandler;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@RestController
@Validated
@RequestMapping("/auth")
public class AuthController {
	private final AuthService authService;
	private final JwtTokenService jwtTokenService;
	private final boolean secureCookie;

	public AuthController(
			AuthService authService,
			JwtTokenService jwtTokenService,
			@Value("${app.jwt.cookie-secure}") boolean secureCookie) {
		this.authService = authService;
		this.jwtTokenService = jwtTokenService;
		this.secureCookie = secureCookie;
	}

	@PostMapping("/register")
	public ResponseEntity<AuthDtos.AuthResponse> register(@Valid @RequestBody AuthDtos.RegisterRequest request) {
		return withAuthCookie(authService.register(request));
	}

	@PostMapping("/login")
	public ResponseEntity<AuthDtos.AuthResponse> login(@Valid @RequestBody AuthDtos.LoginRequest request) {
		return withAuthCookie(authService.login(request));
	}

	@GetMapping("/me")
	public AuthDtos.UserResponse currentUser(Principal principal) {
		return authService.currentUser(principal.getName());
	}

	@GetMapping("/csrf")
	public AuthDtos.CsrfTokenResponse csrfToken(CsrfToken csrfToken) {
		return new AuthDtos.CsrfTokenResponse(csrfToken.getToken());
	}

	@PostMapping("/forgot-password")
	public AuthDtos.MessageResponse forgotPassword(
			@Valid @RequestBody AuthDtos.ForgotPasswordRequest request) {
		return authService.requestPasswordReset(request.email());
	}

	@PostMapping("/reset-password")
	public AuthDtos.MessageResponse resetPassword(
			@Valid @RequestBody AuthDtos.ResetPasswordRequest request) {
		authService.resetPassword(request);
		return new AuthDtos.MessageResponse("Your password has been updated.");
	}

	@PostMapping("/logout")
	public ResponseEntity<Void> logout(HttpServletResponse response) {
		ResponseCookie cookie = ResponseCookie.from(OAuthLoginSuccessHandler.AUTH_COOKIE, "")
				.httpOnly(true)
				.secure(secureCookie)
				.sameSite("Lax")
				.path("/api")
				.maxAge(0)
				.build();
		return ResponseEntity.noContent().header(HttpHeaders.SET_COOKIE, cookie.toString()).build();
	}

	private ResponseEntity<AuthDtos.AuthResponse> withAuthCookie(AuthDtos.AuthResponse authResponse) {
		ResponseCookie cookie = ResponseCookie.from(OAuthLoginSuccessHandler.AUTH_COOKIE, authResponse.token())
				.httpOnly(true)
				.secure(secureCookie)
				.sameSite("Lax")
				.path("/api")
				.maxAge(jwtTokenService.getExpirationSeconds())
				.build();
		return ResponseEntity.ok()
				.header(HttpHeaders.SET_COOKIE, cookie.toString())
				.body(authResponse);
	}
}
