package com.schedly.api.auth;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.schedly.api.user.User;

import org.springframework.stereotype.Component;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@Component
public class JwtTokenProvider {

	private static final Base64.Encoder BASE64_URL = Base64.getUrlEncoder().withoutPadding();
	private static final Base64.Decoder BASE64_URL_DECODER = Base64.getUrlDecoder();
	private static final String HMAC_SHA256 = "HmacSHA256";
	private static final TypeReference<Map<String, Object>> CLAIMS_TYPE = new TypeReference<>() {
	};

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

	public Optional<AuthenticatedUser> authenticate(String token) {
		try {
			final var parts = token.split("\\.");
			if (parts.length != 3) {
				return Optional.empty();
			}

			final var unsignedToken = parts[0] + "." + parts[1];
			if (!constantTimeEquals(parts[2], sign(unsignedToken))) {
				return Optional.empty();
			}

			final var claims = decodeClaims(parts[1]);
			if (expiresAt(claims).isBefore(Instant.now())) {
				return Optional.empty();
			}

			final var userId = UUID.fromString(requiredString(claims, "sub"));
			final var email = requiredString(claims, "email");
			return Optional.of(new AuthenticatedUser(userId, email));
		}
		catch (RuntimeException exception) {
			return Optional.empty();
		}
	}

	private String encodeJson(Map<String, Object> value) {
		try {
			return BASE64_URL.encodeToString(objectMapper.writeValueAsBytes(value));
		}
		catch (Exception exception) {
			throw new IllegalStateException("Failed to encode JWT payload", exception);
		}
	}

	private Map<String, Object> decodeClaims(String encodedClaims) {
		try {
			return objectMapper.readValue(BASE64_URL_DECODER.decode(encodedClaims), CLAIMS_TYPE);
		}
		catch (Exception exception) {
			throw new IllegalArgumentException("Failed to decode JWT claims", exception);
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

	private static boolean constantTimeEquals(String left, String right) {
		return MessageDigest.isEqual(
			left.getBytes(StandardCharsets.UTF_8),
			right.getBytes(StandardCharsets.UTF_8)
		);
	}

	private static Instant expiresAt(Map<String, Object> claims) {
		final var exp = claims.get("exp");
		if (exp instanceof Number number) {
			return Instant.ofEpochSecond(number.longValue());
		}
		throw new IllegalArgumentException("JWT exp claim is missing");
	}

	private static String requiredString(Map<String, Object> claims, String name) {
		final var value = claims.get(name);
		if (value instanceof String text && !text.isBlank()) {
			return text;
		}
		throw new IllegalArgumentException("JWT " + name + " claim is missing");
	}

	record TokenIssue(String token, long expiresInSeconds) {
	}

}
