package com.schedly.api.schedule;

import java.time.Instant;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ScheduleRequest(
	@NotBlank @Size(max = 120) String title,
	@NotNull Instant startAt,
	@NotNull Instant endAt,
	@Size(max = 1000) String memo
) {

	public ScheduleRequest {
		title = trim(title);
		memo = trimToNull(memo);
	}

	private static String trim(String value) {
		return value == null ? null : value.trim();
	}

	private static String trimToNull(String value) {
		final var text = trim(value);
		return text == null || text.isEmpty() ? null : text;
	}

}
