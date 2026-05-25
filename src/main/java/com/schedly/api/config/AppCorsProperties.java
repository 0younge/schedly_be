package com.schedly.api.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

import jakarta.validation.constraints.NotEmpty;

@ConfigurationProperties(prefix = "app.cors")
public record AppCorsProperties(@NotEmpty List<String> allowedOriginPatterns) {
}
