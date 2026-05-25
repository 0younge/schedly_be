package com.schedly.api.auth;

public class InvalidCredentialsException extends RuntimeException {

	InvalidCredentialsException() {
		super("Email or password is invalid");
	}

}
