package com.schedly.api.schedule;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.schedly.api.user.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ScheduleService {

	private final ScheduleRepository scheduleRepository;
	private final UserRepository userRepository;

	public ScheduleService(ScheduleRepository scheduleRepository, UserRepository userRepository) {
		this.scheduleRepository = scheduleRepository;
		this.userRepository = userRepository;
	}

	@Transactional(readOnly = true)
	public List<ScheduleResponse> findOverlapping(UUID userId, Instant from, Instant to) {
		validateRange(from, to);

		return scheduleRepository.findByUserIdAndStartAtLessThanAndEndAtGreaterThanOrderByStartAtAsc(userId, to, from)
			.stream()
			.map(ScheduleResponse::from)
			.toList();
	}

	@Transactional
	public ScheduleResponse create(UUID userId, ScheduleRequest request) {
		validateRange(request.startAt(), request.endAt());

		final var user = userRepository.findById(userId)
			.orElseThrow(() -> new IllegalArgumentException("User does not exist"));
		final var schedule = scheduleRepository.saveAndFlush(
			Schedule.create(user, request.title(), request.startAt(), request.endAt(), request.memo())
		);

		return ScheduleResponse.from(schedule);
	}

	private static void validateRange(Instant from, Instant to) {
		if (!to.isAfter(from)) {
			throw new IllegalArgumentException("endAt must be after startAt");
		}
	}

}
