package com.library.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

@Schema(description = "Book data transfer object")
public class BookDto {

	@Schema(description = "Book identifier", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
	private Long id;

	@NotBlank
	@Size(max = 255)
	@Schema(description = "Book title", example = "Pride and Prejudice")
	private String title;

	@NotBlank
	@Size(max = 20)
	@Schema(description = "International Standard Book Number", example = "9780141439518")
	private String isbn;

	@Positive
	@Schema(description = "Year the book was published", example = "1813")
	private Integer publishedYear;

	@NotNull
	@Positive
	@Schema(description = "Identifier of the book's author", example = "1")
	private Long authorId;

	@Schema(description = "Author display name", example = "Andrew Hunt", accessMode = Schema.AccessMode.READ_ONLY)
	private String authorName;

	@Schema(
			description = "Whether the book can currently be borrowed. Controlled by borrow/return, not by create/update.",
			example = "true",
			accessMode = Schema.AccessMode.READ_ONLY)
	private boolean available;

	@Schema(description = "Cover image file identifier, if attached", accessMode = Schema.AccessMode.READ_ONLY)
	private Long coverFileId;

	@Schema(description = "Original cover image filename", accessMode = Schema.AccessMode.READ_ONLY)
	private String coverFileName;

	@Schema(description = "Preface PDF file identifier, if attached", accessMode = Schema.AccessMode.READ_ONLY)
	private Long prefaceFileId;

	@Schema(description = "Original preface filename", accessMode = Schema.AccessMode.READ_ONLY)
	private String prefaceFileName;

	public BookDto() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getIsbn() {
		return isbn;
	}

	public void setIsbn(String isbn) {
		this.isbn = isbn;
	}

	public Integer getPublishedYear() {
		return publishedYear;
	}

	public void setPublishedYear(Integer publishedYear) {
		this.publishedYear = publishedYear;
	}

	public Long getAuthorId() {
		return authorId;
	}

	public void setAuthorId(Long authorId) {
		this.authorId = authorId;
	}

	public String getAuthorName() {
		return authorName;
	}

	public void setAuthorName(String authorName) {
		this.authorName = authorName;
	}

	public boolean isAvailable() {
		return available;
	}

	public void setAvailable(boolean available) {
		this.available = available;
	}

	public Long getCoverFileId() {
		return coverFileId;
	}

	public void setCoverFileId(Long coverFileId) {
		this.coverFileId = coverFileId;
	}

	public String getCoverFileName() {
		return coverFileName;
	}

	public void setCoverFileName(String coverFileName) {
		this.coverFileName = coverFileName;
	}

	public Long getPrefaceFileId() {
		return prefaceFileId;
	}

	public void setPrefaceFileId(Long prefaceFileId) {
		this.prefaceFileId = prefaceFileId;
	}

	public String getPrefaceFileName() {
		return prefaceFileName;
	}

	public void setPrefaceFileName(String prefaceFileName) {
		this.prefaceFileName = prefaceFileName;
	}
}
