package com.library.dto;

import com.library.entity.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request to change a user's role")
public class UpdateUserRoleRequest {

	@NotNull(message = "Role is required")
	@Schema(description = "Target role", example = "ADMIN", allowableValues = {"USER", "ADMIN"})
	private Role role;

	public UpdateUserRoleRequest() {
	}

	public UpdateUserRoleRequest(Role role) {
		this.role = role;
	}

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}
}
