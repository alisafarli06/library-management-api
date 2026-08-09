package com.library.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "app.file")
public class FileStorageProperties {

	/**
	 * Directory where uploaded files are stored (relative or absolute).
	 */
	private String storageDirectory = "uploads";

	/**
	 * Maximum upload size in bytes (default 10 MB).
	 */
	private long maxSize = 10_485_760L;

	/**
	 * Allowed MIME types (allowlist).
	 */
	private List<String> allowedContentTypes = new ArrayList<>(List.of(
			"image/jpeg",
			"image/png",
			"application/pdf"
	));

	public String getStorageDirectory() {
		return storageDirectory;
	}

	public void setStorageDirectory(String storageDirectory) {
		this.storageDirectory = storageDirectory;
	}

	public long getMaxSize() {
		return maxSize;
	}

	public void setMaxSize(long maxSize) {
		this.maxSize = maxSize;
	}

	public List<String> getAllowedContentTypes() {
		return allowedContentTypes;
	}

	public void setAllowedContentTypes(List<String> allowedContentTypes) {
		this.allowedContentTypes = allowedContentTypes;
	}
}
