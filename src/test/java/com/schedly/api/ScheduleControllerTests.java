package com.schedly.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ScheduleControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	void rejectsScheduleRequestsWithoutToken() throws Exception {
		mockMvc.perform(get("/api/schedules")
				.param("from", "2026-05-26T00:00:00Z")
				.param("to", "2026-05-27T00:00:00Z"))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
	}

	@Test
	void rejectsScheduleRequestsWithInvalidToken() throws Exception {
		mockMvc.perform(get("/api/schedules")
				.header(HttpHeaders.AUTHORIZATION, "Bearer invalid-token")
				.param("from", "2026-05-26T00:00:00Z")
				.param("to", "2026-05-27T00:00:00Z"))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
	}

	@Test
	void createsAndFindsSchedulesForSignedInUserOnly() throws Exception {
		final var ownerToken = signup("schedule-owner@example.com");
		final var otherToken = signup("schedule-other@example.com");

		createSchedule(ownerToken, "Product review", "2026-05-26T09:00:00Z", "2026-05-26T10:00:00Z");
		createSchedule(otherToken, "Other user schedule", "2026-05-26T09:30:00Z", "2026-05-26T10:30:00Z");

		mockMvc.perform(get("/api/schedules")
				.header(HttpHeaders.AUTHORIZATION, bearer(ownerToken))
				.param("from", "2026-05-26T00:00:00Z")
				.param("to", "2026-05-27T00:00:00Z"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[0].title").value("Product review"))
			.andExpect(jsonPath("$[0].memo").value("MVP"));
	}

	@Test
	void rejectsScheduleWhenEndIsNotAfterStart() throws Exception {
		final var token = signup("invalid-schedule@example.com");

		mockMvc.perform(post("/api/schedules")
				.header(HttpHeaders.AUTHORIZATION, bearer(token))
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "title": "Invalid",
					  "startAt": "2026-05-26T09:00:00Z",
					  "endAt": "2026-05-26T09:00:00Z"
					}
					"""))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
	}

	private String signup(String email) throws Exception {
		final var response = mockMvc.perform(post("/api/auth/signup")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "name": "Owner",
					  "email": "%s",
					  "password": "password123"
					}
					""".formatted(email)))
			.andExpect(status().isCreated())
			.andReturn()
			.getResponse()
			.getContentAsString();

		final var body = objectMapper.readValue(response, Map.class);
		final var token = body.get("accessToken");
		assertThat(token).isInstanceOf(String.class);
		return (String) token;
	}

	private void createSchedule(String token, String title, String startAt, String endAt) throws Exception {
		mockMvc.perform(post("/api/schedules")
				.header(HttpHeaders.AUTHORIZATION, bearer(token))
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
					  "title": "%s",
					  "startAt": "%s",
					  "endAt": "%s",
					  "memo": " MVP "
					}
					""".formatted(title, startAt, endAt)))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.id").isNotEmpty())
			.andExpect(jsonPath("$.title").value(title));
	}

	private static String bearer(String token) {
		return "Bearer " + token;
	}

}
