package com.schedly.api.auth;

import com.schedly.api.user.User;

public record AuthResponse(
	String tokenType,
	String accessToken,
	long expiresInSeconds,
	AuthUserResponse user
) {

	static AuthResponse bearer(JwtTokenProvider.TokenIssue tokenIssue, User user) {
		return new AuthResponse("Bearer", tokenIssue.token(), tokenIssue.expiresInSeconds(), AuthUserResponse.from(user));
	}

}
