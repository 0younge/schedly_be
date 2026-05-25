package com.schedly.api.auth;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.schedly.api.user.User;

import org.springframework.stereotype.Component;

import tools.jackson.databind.ObjectMapper;

@Component
public class JwtTokenProvider {

	private static final Base64.Encoder BASE64_URL = Base64.getUrlEncoder().withoutPadding();
	private static final String HMAC_SHA256 = "HmacSHA256";

	private final JwtProperties properties;
	private final ObjectMapper objectMapper;
	private final SecretKeySpec secretKey;

	public JwtTokenProvider(JwtProperties properties, ObjectMapper objectMapper) {
		this.properties = properties;
		this.objectMapper = objectMapper;
		secretKey = new SecretKeySpec(properties.secret().getBytes(StandardCharsets.UTF_8), HMAC_SHA256);
	}

	TokenIssue issue(User user) {
		final var issuedAt = Instant.now();
		final var expiresAt = issuedAt.plusSeconds(properties.expiresInSeconds());
		final var header = encodeJson(Map.of(
			"alg", "HS256",
			"typ", "JWT"
		));
		final var claims = encodeJson(Map.of(
			"sub", user.getId().toString(),
			"email", user.getEmail(),
			"iat", issuedAt.getEpochSecond(),
			"exp", expiresAt.getEpochSecond()
		));
		final var unsignedToken = header + "." + claims;
		return new TokenIssue(unsignedToken + "." + sign(unsignedToken), properties.expiresInSeconds());
	}

	private String encodeJson(Map<String, Object> value) {
		try {
			return BASE64_URL.encodeToString(objectMapper.writeValueAsBytes(value));
		}
		catch (Exception exception) {
			throw new IllegalStateException("Failed to encode JWT payload", exception);
		}
	}

	private String sign(String value) {
		try {
			final var mac = Mac.getInstance(HMAC_SHA256);
			mac.init(secretKey);
			return BASE64_URL.encodeToString(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
		}
		catch (Exception exception) {
			throw new IllegalStateException("Failed to sign JWT", exception);
		}
	}

	record TokenIssue(String token, long expiresInSeconds) {
	}

}
