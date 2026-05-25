package com.schedly.api.common;

import com.schedly.api.auth.EmailAlreadyUsedException;
import com.schedly.api.auth.InvalidCredentialsException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

	@ExceptionHandler(EmailAlreadyUsedException.class)
	ResponseEntity<ApiErrorResponse> handleEmailAlreadyUsed(EmailAlreadyUsedException exception) {
		return ResponseEntity.status(HttpStatus.CONFLICT)
			.body(ApiErrorResponse.of("EMAIL_ALREADY_USED", exception.getMessage()));
	}

	@ExceptionHandler(InvalidCredentialsException.class)
	ResponseEntity<ApiErrorResponse> handleInvalidCredentials(InvalidCredentialsException exception) {
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
			.body(ApiErrorResponse.of("INVALID_CREDENTIALS", exception.getMessage()));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException exception) {
		final var message = exception.getBindingResult()
			.getFieldErrors()
			.stream()
			.findFirst()
			.map(error -> error.getField() + " " + error.getDefaultMessage())
			.orElse("Request is invalid");

		return ResponseEntity.badRequest()
			.body(ApiErrorResponse.of("VALIDATION_ERROR", message));
	}

	@ExceptionHandler(IllegalArgumentException.class)
	ResponseEntity<ApiErrorResponse> handleIllegalArgument(IllegalArgumentException exception) {
		return ResponseEntity.badRequest()
			.body(ApiErrorResponse.of("INVALID_REQUEST", exception.getMessage()));
	}

}
