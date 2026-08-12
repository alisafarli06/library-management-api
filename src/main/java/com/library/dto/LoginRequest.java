package com.library.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Login credentials")
public class LoginRequest {

	@NotBlank
	@Email
	@Schema(description = "Registered email address", example = "jane.doe@example.com")
	private String email;

	@NotBlank
	@Schema(description = "Account password", example = "SecurePass123", format = "password")
	private String password;

	public LoginRequest() {
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
