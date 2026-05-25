package com.schedly.api.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignupRequest(
	@NotBlank @Size(max = 100) String name,
	@NotBlank @Email @Size(max = 320) String email,
	@NotBlank @Size(min = 8, max = 72) String password
) {

	public SignupRequest {
		name = trim(name);
		email = trim(email);
	}

	private static String trim(String value) {
		return value == null ? null : value.trim();
	}

}
