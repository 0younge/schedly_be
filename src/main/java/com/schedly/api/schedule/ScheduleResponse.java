package com.schedly.api.schedule;

import java.time.Instant;
import java.util.UUID;

public record ScheduleResponse(
	UUID id,
	String title,
	Instant startAt,
	Instant endAt,
	String memo
) {

	static ScheduleResponse from(Schedule schedule) {
		return new ScheduleResponse(
			schedule.getId(),
			schedule.getTitle(),
			schedule.getStartAt(),
			schedule.getEndAt(),
			schedule.getMemo()
		);
	}

}
