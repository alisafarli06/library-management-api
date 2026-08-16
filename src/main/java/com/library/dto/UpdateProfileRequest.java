package com.library.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Update authenticated user profile request")
public class UpdateProfileRequest {

	@NotBlank
	@Size(max = 255)
	@Schema(description = "Updated full name", example = "Ali Safarli")
	private String name;

	public UpdateProfileRequest() {
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
