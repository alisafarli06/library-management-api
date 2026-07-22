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
}
