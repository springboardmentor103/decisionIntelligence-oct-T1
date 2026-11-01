package com.decisionhub.security;

import java.io.IOException;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import com.decisionhub.user.AccountRole;
import com.decisionhub.user.AppUser;
import com.decisionhub.user.AppUserRepository;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class OAuthLoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
	public static final String AUTH_COOKIE = "decisionhub_auth";

	private final AppUserRepository users;
	private final PasswordEncoder passwordEncoder;
	private final JwtTokenService tokenService;
	private final String frontendUrl;
	private final boolean secureCookie;

	public OAuthLoginSuccessHandler(
			AppUserRepository users,
			PasswordEncoder passwordEncoder,
			JwtTokenService tokenService,
			@Value("${app.frontend-url}") String frontendUrl,
			@Value("${app.jwt.cookie-secure}") boolean secureCookie) {
		this.users = users;
		this.passwordEncoder = passwordEncoder;
		this.tokenService = tokenService;
		this.frontendUrl = frontendUrl;
		this.secureCookie = secureCookie;
	}

	@Override
	public void onAuthenticationSuccess(
			HttpServletRequest request,
			HttpServletResponse response,
			Authentication authentication) throws IOException, ServletException {
		OAuth2User principal = (OAuth2User) authentication.getPrincipal();
		String email = principal.getAttribute("email");
		String name = principal.getAttribute("name");
		if (email == null || email.isBlank()) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "OAuth provider did not return an email address.");
			return;
		}

		String normalizedEmail = email.trim().toLowerCase();
		AppUser user = users.findByEmail(normalizedEmail).orElseGet(() -> users.save(
				new AppUser(name == null || name.isBlank() ? normalizedEmail : name,
						normalizedEmail,
						passwordEncoder.encode(UUID.randomUUID().toString()),
						AccountRole.USER)));

		String cookie = AUTH_COOKIE + "=" + tokenService.issue(user)
				+ "; Path=/api; HttpOnly; SameSite=Lax; Max-Age=" + tokenService.getExpirationSeconds()
				+ (secureCookie ? "; Secure" : "");
		response.addHeader("Set-Cookie", cookie);
		getRedirectStrategy().sendRedirect(
				request,
				response,
				UriComponentsBuilder.fromUriString(frontendUrl)
						.path("/oauth/callback")
						.build()
						.toUriString());
	}
}
