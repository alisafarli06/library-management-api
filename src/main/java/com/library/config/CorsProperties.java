package com.library.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.cors")
public class CorsProperties {

	/**
	 * Comma-separated browser origins allowed to call the API.
	 * Dev defaults to local Vite origins. Prod uses {@code FRONTEND_ORIGIN}
	 * (with {@code CORS_ALLOWED_ORIGINS} as a fallback).
	 */
	private String allowedOrigins = "";

	public String getAllowedOrigins() {
		return allowedOrigins;
	}

	public void setAllowedOrigins(String allowedOrigins) {
		this.allowedOrigins = allowedOrigins;
	}
}
