package com.library.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex) {
		ErrorResponse body = new ErrorResponse(
				Instant.now(),
				HttpStatus.NOT_FOUND.value(),
				"Not Found",
				ex.getMessage()
		);
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
	}

	@ExceptionHandler(ConflictException.class)
	public ResponseEntity<ErrorResponse> handleConflict(ConflictException ex) {
		ErrorResponse body = new ErrorResponse(
				Instant.now(),
				HttpStatus.CONFLICT.value(),
				"Conflict",
				ex.getMessage()
		);
		return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
		ErrorResponse body = new ErrorResponse(
				Instant.now(),
				HttpStatus.CONFLICT.value(),
				"Conflict",
				"Data integrity violation"
		);
		return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
		return buildValidationErrorResponse(ex.getBindingResult().getFieldErrors(), ex.getBindingResult().getGlobalErrors());
	}

	@ExceptionHandler(BindException.class)
	public ResponseEntity<ErrorResponse> handleBindException(BindException ex) {
		return buildValidationErrorResponse(ex.getBindingResult().getFieldErrors(), ex.getBindingResult().getGlobalErrors());
	}

	@ExceptionHandler(BadCredentialsException.class)
	public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex) {
		ErrorResponse body = new ErrorResponse(
				Instant.now(),
				HttpStatus.UNAUTHORIZED.value(),
				"Unauthorized",
				"Invalid email or password"
		);
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
	}

	@ExceptionHandler(AuthenticationException.class)
	public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException ex) {
		ErrorResponse body = new ErrorResponse(
				Instant.now(),
				HttpStatus.UNAUTHORIZED.value(),
				"Unauthorized",
				ex.getMessage()
		);
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleUnexpectedError(Exception ex) {
		ErrorResponse body = new ErrorResponse(
				Instant.now(),
				HttpStatus.INTERNAL_SERVER_ERROR.value(),
				"Internal Server Error",
				"An unexpected error occurred"
		);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
	}

	private ResponseEntity<ErrorResponse> buildValidationErrorResponse(
			Iterable<FieldError> fieldErrors,
			Iterable<ObjectError> globalErrors) {
		Map<String, String> errors = new HashMap<>();
		for (FieldError fieldError : fieldErrors) {
			errors.put(fieldError.getField(), fieldError.getDefaultMessage());
		}
		for (ObjectError globalError : globalErrors) {
			errors.put(globalError.getObjectName(), globalError.getDefaultMessage());
		}

		ErrorResponse body = new ErrorResponse(
				Instant.now(),
				HttpStatus.BAD_REQUEST.value(),
				"Bad Request",
				"Validation failed",
				errors
		);
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
	}
}
