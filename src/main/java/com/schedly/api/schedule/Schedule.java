package com.schedly.api.schedule;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import com.schedly.api.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "schedules")
public class Schedule {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(nullable = false, updatable = false)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(nullable = false, length = 120)
	private String title;

	@Column(name = "start_at", nullable = false)
	private Instant startAt;

	@Column(name = "end_at", nullable = false)
	private Instant endAt;

	@Column(length = 1000)
	private String memo;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	protected Schedule() {
	}

	private Schedule(User user, String title, Instant startAt, Instant endAt, String memo) {
		this.user = Objects.requireNonNull(user, "user");
		this.title = requireText(title, "title");
		this.startAt = Objects.requireNonNull(startAt, "startAt");
		this.endAt = Objects.requireNonNull(endAt, "endAt");
		if (!endAt.isAfter(startAt)) {
			throw new IllegalArgumentException("endAt must be after startAt");
		}
		this.memo = normalizeMemo(memo);
	}

	public static Schedule create(User user, String title, Instant startAt, Instant endAt, String memo) {
		return new Schedule(user, title, startAt, endAt, memo);
	}

	public UUID getId() {
		return id;
	}

	public User getUser() {
		return user;
	}

	public String getTitle() {
		return title;
	}

	public Instant getStartAt() {
		return startAt;
	}

	public Instant getEndAt() {
		return endAt;
	}

	public String getMemo() {
		return memo;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	@PrePersist
	void prePersist() {
		final var now = Instant.now();
		createdAt = now;
		updatedAt = now;
	}

	@PreUpdate
	void preUpdate() {
		updatedAt = Instant.now();
	}

	private static String requireText(String value, String fieldName) {
		final var text = Objects.requireNonNull(value, fieldName).trim();
		if (text.isEmpty()) {
			throw new IllegalArgumentException(fieldName + " must not be blank");
		}
		return text;
	}

	private static String normalizeMemo(String value) {
		if (value == null) {
			return null;
		}
		final var text = value.trim();
		return text.isEmpty() ? null : text;
	}

}
