package com.library.exception;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.Map;

@Schema(description = "Standard API error response")
public class ErrorResponse {

	@Schema(description = "Time the error occurred", example = "2026-08-12T12:00:00Z")
	private Instant timestamp;

	@Schema(description = "HTTP status code", example = "404")
	private int status;

	@Schema(description = "HTTP status reason phrase", example = "Not Found")
	private String error;

	@Schema(description = "Human-readable error message", example = "Book not found with id: 99")
	private String message;

	@Schema(description = "Field-level validation errors (present for 400 validation failures)")
	private Map<String, String> fieldErrors;

	public ErrorResponse() {
	}

	public ErrorResponse(Instant timestamp, int status, String error, String message) {
		this.timestamp = timestamp;
		this.status = status;
		this.error = error;
		this.message = message;
	}

	public ErrorResponse(Instant timestamp, int status, String error, String message, Map<String, String> fieldErrors) {
		this.timestamp = timestamp;
		this.status = status;
		this.error = error;
		this.message = message;
		this.fieldErrors = fieldErrors;
	}

	public Instant getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Instant timestamp) {
		this.timestamp = timestamp;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Map<String, String> getFieldErrors() {
		return fieldErrors;
	}

	public void setFieldErrors(Map<String, String> fieldErrors) {
		this.fieldErrors = fieldErrors;
	}
}
