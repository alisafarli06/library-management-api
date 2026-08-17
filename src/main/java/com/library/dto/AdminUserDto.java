package com.library.dto;

import com.library.entity.AccountStatus;
import com.library.entity.Role;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Admin-facing user account. Password hashes are never included.")
public class AdminUserDto {

	@Schema(description = "User identifier", example = "1")
	private Long id;

	@Schema(description = "User full name", example = "Ali Safarli")
	private String fullName;

	@Schema(description = "User email address", example = "alisafarli@gmail.com")
	private String email;

	@Schema(description = "Assigned role", example = "ADMIN")
	private Role role;

	@Schema(description = "Account status", example = "ACTIVE")
	private AccountStatus status;

	@Schema(description = "Account creation timestamp")
	private Instant createdAt;

	public AdminUserDto() {
	}

	public AdminUserDto(Long id, String fullName, String email, Role role, AccountStatus status, Instant createdAt) {
		this.id = id;
		this.fullName = fullName;
		this.email = email;
		this.role = role;
		this.status = status;
		this.createdAt = createdAt;
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

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}
}
