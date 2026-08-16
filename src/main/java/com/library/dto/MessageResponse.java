package com.library.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Simple API message response")
public class MessageResponse {

	@Schema(description = "Human-readable message", example = "Password changed successfully.")
	private String message;

	public MessageResponse() {
	}

	public MessageResponse(String message) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
