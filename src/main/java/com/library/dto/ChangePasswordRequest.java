package com.library.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Change password request for the authenticated user")
public class ChangePasswordRequest {

	@NotBlank
	@Schema(description = "Current account password", example = "OldPassword1")
	private String currentPassword;

	@NotBlank
	@Size(min = 8, max = 72)
	@Schema(description = "New password (8–72 characters)", example = "NewPassword1")
	private String newPassword;

	public ChangePasswordRequest() {
	}

	public String getCurrentPassword() {
		return currentPassword;
	}

	public void setCurrentPassword(String currentPassword) {
		this.currentPassword = currentPassword;
	}

	public String getNewPassword() {
		return newPassword;
	}

	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}
}
