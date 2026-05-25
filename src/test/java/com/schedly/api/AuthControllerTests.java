package com.schedly.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void signupCreatesUserAndReturnsBearerToken() throws Exception {
		mockMvc.perform(post("/api/auth/signup")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "name": "Owner",
					  "email": " OWNER@Example.COM ",
					  "password": "password123"
					}
					"""))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.tokenType").value("Bearer"))
			.andExpect(jsonPath("$.accessToken").isNotEmpty())
			.andExpect(jsonPath("$.expiresInSeconds").value(3600))
			.andExpect(jsonPath("$.user.id").isNotEmpty())
			.andExpect(jsonPath("$.user.email").value("owner@example.com"))
			.andExpect(jsonPath("$.user.name").value("Owner"));
	}

	@Test
	void loginReturnsBearerTokenForExistingUser() throws Exception {
		signup("login@example.com", "password123");

		mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "email": "login@example.com",
					  "password": "password123"
					}
					"""))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.tokenType").value("Bearer"))
			.andExpect(jsonPath("$.accessToken").isNotEmpty())
			.andExpect(jsonPath("$.user.email").value("login@example.com"));
	}

	@Test
	void signupRejectsDuplicateEmail() throws Exception {
		signup("duplicate@example.com", "password123");

		mockMvc.perform(post("/api/auth/signup")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "name": "Owner",
					  "email": "duplicate@example.com",
					  "password": "password123"
					}
					"""))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.code").value("EMAIL_ALREADY_USED"));
	}

	@Test
	void loginRejectsInvalidPassword() throws Exception {
		signup("invalid-password@example.com", "password123");

		mockMvc.perform(post("/api/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "email": "invalid-password@example.com",
					  "password": "password456"
					}
					"""))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));
	}

	private void signup(String email, String password) throws Exception {
		mockMvc.perform(post("/api/auth/signup")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "name": "Owner",
					  "email": "%s",
					  "password": "%s"
					}
					""".formatted(email, password)))
			.andExpect(status().isCreated());
	}

}
