package com.library.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Uploaded file metadata (no internal storage paths)")
public class FileMetadataDto {

	@Schema(description = "File identifier", example = "1")
	private Long id;

	@Schema(description = "Original client filename", example = "cover.pdf")
	private String originalFilename;

	@Schema(description = "Detected content type", example = "application/pdf")
	private String contentType;

	@Schema(description = "File size in bytes", example = "2048")
	private long size;

	@Schema(description = "Upload timestamp")
	private Instant createdAt;

	public FileMetadataDto() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getOriginalFilename() {
		return originalFilename;
	}

	public void setOriginalFilename(String originalFilename) {
		this.originalFilename = originalFilename;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}
}
