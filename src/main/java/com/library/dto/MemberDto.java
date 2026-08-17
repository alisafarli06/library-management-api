package com.library.dto;

import com.library.entity.AccountStatus;
import com.library.entity.Role;
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

	@Schema(
			description = "Number of active (not yet returned) loans for this member",
			example = "1",
			accessMode = Schema.AccessMode.READ_ONLY)
	private Long activeLoanCount;

	@Schema(description = "Linked user account id, if this member can log in", example = "12", accessMode = Schema.AccessMode.READ_ONLY)
	private Long userId;

	@Schema(description = "Linked user role", example = "USER", accessMode = Schema.AccessMode.READ_ONLY)
	private Role role;

	@Schema(description = "Linked user account status", example = "ACTIVE", accessMode = Schema.AccessMode.READ_ONLY)
	private AccountStatus status;

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

	public Long getActiveLoanCount() {
		return activeLoanCount;
	}

	public void setActiveLoanCount(Long activeLoanCount) {
		this.activeLoanCount = activeLoanCount;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	public AccountStatus getStatus() {
		return status;
	}

	public void setStatus(AccountStatus status) {
		this.status = status;
	}
}
