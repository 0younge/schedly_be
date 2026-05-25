package com.schedly.api.auth;

import org.springframework.boot.context.properties.ConfigurationProperties;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(
	@NotBlank String secret,
	@Positive long expiresInSeconds
) {
}
