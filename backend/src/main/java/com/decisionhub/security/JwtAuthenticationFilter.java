package com.decisionhub.security;

import java.io.IOException;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
	private final JwtTokenService tokenService;
	private final UserDetailsService userDetailsService;

	public JwtAuthenticationFilter(JwtTokenService tokenService, UserDetailsService userDetailsService) {
		this.tokenService = tokenService;
		this.userDetailsService = userDetailsService;
	}

	@Override
	protected void doFilterInternal(
			HttpServletRequest request,
			HttpServletResponse response,
			FilterChain filterChain) throws ServletException, IOException {
		String token = readBearerToken(request);
		if (token == null) {
			token = readCookieToken(request);
		}

		if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
			try {
				String subject = tokenService.parse(token).getSubject();
				var userDetails = userDetailsService.loadUserByUsername(subject);
				var authentication = new UsernamePasswordAuthenticationToken(
						userDetails, null, userDetails.getAuthorities());
				authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
				SecurityContextHolder.getContext().setAuthentication(authentication);
			} catch (JwtException | UsernameNotFoundException exception) {
				response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired authentication token.");
				return;
			}
		}

		filterChain.doFilter(request, response);
	}

	private String readBearerToken(HttpServletRequest request) {
		String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (authorization != null && authorization.startsWith("Bearer ")) {
			return authorization.substring(7);
		}
		return null;
	}

	private String readCookieToken(HttpServletRequest request) {
		Cookie[] cookies = request.getCookies();
		if (cookies == null) {
			return null;
		}
		for (Cookie cookie : cookies) {
			if (OAuthLoginSuccessHandler.AUTH_COOKIE.equals(cookie.getName())) {
				return cookie.getValue();
			}
		}
		return null;
	}
}
