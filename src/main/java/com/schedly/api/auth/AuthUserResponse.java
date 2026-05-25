package com.schedly.api.auth;

import java.util.UUID;

import com.schedly.api.user.User;

public record AuthUserResponse(UUID id, String email, String name) {

	static AuthUserResponse from(User user) {
		return new AuthUserResponse(user.getId(), user.getEmail(), user.getName());
	}

}
