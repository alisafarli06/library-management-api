package com.library.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Authenticated user profile (no password or role)")
public class UserProfileDto {

	@NotBlank
	@Size(max = 255)
	@Schema(description = "User full name", example = "Ali Safarli")
	private String name;

	@NotBlank
	@Email
	@Size(max = 255)
	@Schema(description = "User email address", example = "ali@example.com", accessMode = Schema.AccessMode.READ_ONLY)
	private String email;

	public UserProfileDto() {
	}

	public UserProfileDto(String name, String email) {
		this.name = name;
		this.email = email;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
}
