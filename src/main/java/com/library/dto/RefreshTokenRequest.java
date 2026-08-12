package com.library.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Refresh token payload")
public class RefreshTokenRequest {

	@NotBlank
	@Schema(description = "Valid refresh token issued at login or registration")
	private String refreshToken;

	public RefreshTokenRequest() {
	}

	public String getRefreshToken() {
		return refreshToken;
	}

	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}
}
