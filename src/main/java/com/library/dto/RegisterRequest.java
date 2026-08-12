package com.library.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "User registration payload")
public class RegisterRequest {

	@NotBlank
	@Size(max = 255)
	@Schema(description = "User full name", example = "Jane Doe")
	private String fullName;

	@NotBlank
	@Email
	@Size(max = 255)
	@Schema(description = "Unique email address", example = "jane.doe@example.com")
	private String email;

	@NotBlank
	@Size(min = 8, max = 72)
	@Schema(description = "Account password (8–72 characters)", example = "SecurePass123", format = "password")
	private String password;

	public RegisterRequest() {
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
