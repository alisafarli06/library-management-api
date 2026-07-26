package com.library.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;

@Schema(description = "User data transfer object")
public class UserDto {

	@Schema(description = "User identifier", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
	private Long id;

	@NotBlank
	@Size(max = 255)
	@Schema(description = "User full name", example = "Ali Safarli")
	private String fullName;

	@NotBlank
	@Email
	@Size(max = 255)
	@Schema(description = "User email address", example = "ali.safarli@gmail.com")
	private String email;

	@NotBlank
	@Size(min = 8, max = 72)
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	@Schema(description = "User password (write-only)", example = "password123", accessMode = Schema.AccessMode.WRITE_ONLY)
	private String password;

	@Schema(description = "Account creation timestamp", accessMode = Schema.AccessMode.READ_ONLY)
	private Instant createdAt;

	public UserDto() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}
}
