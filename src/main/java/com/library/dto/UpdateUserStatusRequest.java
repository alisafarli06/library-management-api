package com.library.dto;

import com.library.entity.AccountStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request to block or unblock a user account")
public class UpdateUserStatusRequest {

	@NotNull(message = "blocked is required")
	@Schema(description = "true to block the account, false to unblock", example = "true")
	private Boolean blocked;

	public UpdateUserStatusRequest() {
	}

	public UpdateUserStatusRequest(Boolean blocked) {
		this.blocked = blocked;
	}

	public Boolean getBlocked() {
		return blocked;
	}

	public void setBlocked(Boolean blocked) {
		this.blocked = blocked;
	}
}
