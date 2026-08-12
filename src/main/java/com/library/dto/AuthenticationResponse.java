package com.library.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "JWT token pair returned after successful authentication")
public class AuthenticationResponse {

	@Schema(description = "Short-lived JWT access token for API requests")
	private String accessToken;

	@Schema(description = "Long-lived refresh token used to obtain a new token pair")
	private String refreshToken;

	public AuthenticationResponse() {
	}

	public AuthenticationResponse(String accessToken, String refreshToken) {
		this.accessToken = accessToken;
		this.refreshToken = refreshToken;
	}

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public String getRefreshToken() {
		return refreshToken;
	}

	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}
}
