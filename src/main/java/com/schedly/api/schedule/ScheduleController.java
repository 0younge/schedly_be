package com.schedly.api.schedule;

import java.time.Instant;
import java.util.List;

import com.schedly.api.auth.AuthenticatedUser;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/schedules")
public class ScheduleController {

	private final ScheduleService scheduleService;

	public ScheduleController(ScheduleService scheduleService) {
		this.scheduleService = scheduleService;
	}

	@GetMapping
	List<ScheduleResponse> findSchedules(
		@RequestParam Instant from,
		@RequestParam Instant to,
		Authentication authentication
	) {
		return scheduleService.findOverlapping(currentUser(authentication).id(), from, to);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	ScheduleResponse createSchedule(
		@Valid @RequestBody ScheduleRequest request,
		Authentication authentication
	) {
		return scheduleService.create(currentUser(authentication).id(), request);
	}

	private static AuthenticatedUser currentUser(Authentication authentication) {
		return (AuthenticatedUser) authentication.getPrincipal();
	}

}
