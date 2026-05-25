package com.schedly.api.auth;

public class EmailAlreadyUsedException extends RuntimeException {

	EmailAlreadyUsedException() {
		super("Email is already registered");
	}

}
