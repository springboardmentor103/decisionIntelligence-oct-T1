package com.decisionhub;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.mock.web.MockHttpServletResponse;

import jakarta.servlet.http.Cookie;

import com.jayway.jsonpath.JsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DecisionhubApiApplicationTests {
	@Autowired
	private MockMvc mockMvc;

	@Test
	void contextLoads() {
	}

	@Test
	void registrationAndJwtLoginProtectTheCurrentUserEndpoint() throws Exception {
		String email = "test-" + UUID.randomUUID() + "@example.com";
		String registerBody = """
				{"name":"Decision Tester","email":"%s","password":"SecurePass123"}
				""".formatted(email);

		MvcResult registration = mockMvc.perform(post("/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content(registerBody))
				.andExpect(status().isOk())
				.andExpect(header().exists("Set-Cookie"))
				.andExpect(jsonPath("$.user.role").value("USER"))
				.andReturn();
		assertTrue(registration.getResponse().getHeader("Set-Cookie").contains("HttpOnly"));

		String token = JsonPath.read(registration.getResponse().getContentAsString(), "$.token");

		mockMvc.perform(get("/auth/me").header("Authorization", "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.email").value(email))
				.andExpect(jsonPath("$.roles[0]").value("USER"));
		mockMvc.perform(get("/auth/me")
						.cookie(new jakarta.servlet.http.Cookie("decisionhub_auth", token)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.email").value(email));

		mockMvc.perform(get("/admin/status").header("Authorization", "Bearer " + token))
				.andExpect(status().isForbidden());

		String loginBody = """
				{"email":"%s","password":"SecurePass123"}
				""".formatted(email);
		mockMvc.perform(post("/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content(loginBody))
				.andExpect(status().isOk())
				.andExpect(header().exists("Set-Cookie"));
	}

	@Test
	void invalidCredentialsAreNotAccepted() throws Exception {
		mockMvc.perform(post("/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email":"nobody@example.com","password":"wrong-password"}
								"""))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void passwordResetDoesNotClaimSuccessWhenEmailDeliveryIsUnconfigured() throws Exception {
		mockMvc.perform(post("/auth/forgot-password")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"email":"person@example.com"}
								"""))
				.andExpect(status().isServiceUnavailable())
				.andExpect(jsonPath("$.message").value("Password reset email is not configured."));
	}

	@Test
	void logoutRequiresAndAcceptsTheCsrfToken() throws Exception {
		mockMvc.perform(post("/auth/logout"))
				.andExpect(status().isForbidden());

		MvcResult csrfResult = mockMvc.perform(get("/auth/csrf"))
				.andExpect(status().isOk())
				.andExpect(header().exists("Set-Cookie"))
				.andReturn();
		String csrfToken = JsonPath.read(csrfResult.getResponse().getContentAsString(), "$.token");
		MockHttpServletResponse response = csrfResult.getResponse();
		Cookie csrfCookie = Arrays.stream(response.getCookies())
				.filter(cookie -> "XSRF-TOKEN".equals(cookie.getName()))
				.findFirst()
				.orElseThrow();

		mockMvc.perform(post("/auth/logout")
						.cookie(csrfCookie)
						.header("X-XSRF-TOKEN", csrfToken))
				.andExpect(status().isNoContent())
				.andExpect(header().exists("Set-Cookie"));
	}
}
