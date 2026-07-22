package com.library.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Member data transfer object")
public class MemberDto {

	@Schema(description = "Member identifier", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
	private Long id;

	@NotBlank
	@Size(max = 255)
	@Schema(description = "Member full name", example = "John Smith")
	private String name;

	@NotBlank
	@Email
	@Size(max = 255)
	@Schema(description = "Member email address", example = "john.smith@example.com")
	private String email;

	public MemberDto() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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
