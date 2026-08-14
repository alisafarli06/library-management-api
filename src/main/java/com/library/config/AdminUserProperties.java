package com.library.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.admin")
public class AdminUserProperties {

	/**
	 * Email used for the bootstrap ADMIN user created on first startup.
	 */
	private String email = "admin@library.com";

	/**
	 * Display name for the bootstrap ADMIN user.
	 */
	private String fullName = "Library Admin";

	/**
	 * Raw password for the bootstrap ADMIN user. Prefer {@code ADMIN_PASSWORD}.
	 * Never log this value.
	 */
	private String password;

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
