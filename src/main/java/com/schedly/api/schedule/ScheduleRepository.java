package com.schedly.api.schedule;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ScheduleRepository extends JpaRepository<Schedule, UUID> {

	List<Schedule> findByUserIdAndStartAtLessThanAndEndAtGreaterThanOrderByStartAtAsc(
		UUID userId,
		Instant rangeEnd,
		Instant rangeStart
	);

}
