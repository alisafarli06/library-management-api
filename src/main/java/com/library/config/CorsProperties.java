package com.library.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.cors")
public class CorsProperties {

	/**
	 * Comma-separated browser origins allowed to call the API.
	 * Prefer {@code FRONTEND_ORIGIN} (or {@code CORS_ALLOWED_ORIGINS}).
	 */
	private String allowedOrigins = "";

	public String getAllowedOrigins() {
		return allowedOrigins;
	}

	public void setAllowedOrigins(String allowedOrigins) {
		this.allowedOrigins = allowedOrigins;
	}
}
