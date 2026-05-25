package com.schedly.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;

import com.schedly.api.schedule.Schedule;
import com.schedly.api.schedule.ScheduleRepository;
import com.schedly.api.user.User;
import com.schedly.api.user.UserRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class DomainPersistenceTests {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ScheduleRepository scheduleRepository;

	@Test
	void persistsUserWithNormalizedEmail() {
		final var user = userRepository.saveAndFlush(
			User.create(" OWNER@Example.COM ", "encoded-password", "Owner")
		);

		assertThat(user.getId()).isNotNull();
		assertThat(user.getEmail()).isEqualTo("owner@example.com");
		assertThat(user.getCreatedAt()).isNotNull();
		assertThat(userRepository.existsByEmail("owner@example.com")).isTrue();
	}

	@Test
	void persistsScheduleForUserAndFindsOverlappingRange() {
		final var user = userRepository.saveAndFlush(
			User.create("owner@example.com", "encoded-password", "Owner")
		);
		final var startAt = Instant.parse("2026-05-26T09:00:00Z");
		final var endAt = Instant.parse("2026-05-26T10:00:00Z");

		final var schedule = scheduleRepository.saveAndFlush(
			Schedule.create(user, "Product review", startAt, endAt, " MVP ")
		);
		final var schedules = scheduleRepository
			.findByUserIdAndStartAtLessThanAndEndAtGreaterThanOrderByStartAtAsc(
				user.getId(),
				Instant.parse("2026-05-26T12:00:00Z"),
				Instant.parse("2026-05-26T00:00:00Z")
			);

		assertThat(schedule.getId()).isNotNull();
		assertThat(schedule.getMemo()).isEqualTo("MVP");
		assertThat(schedules).extracting(Schedule::getTitle).containsExactly("Product review");
	}

	@Test
	void rejectsScheduleWhenEndIsNotAfterStart() {
		final var user = User.create("owner@example.com", "encoded-password", "Owner");
		final var startAt = Instant.parse("2026-05-26T09:00:00Z");

		assertThatThrownBy(() -> Schedule.create(user, "Invalid", startAt, startAt, null))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("endAt must be after startAt");
	}

}
