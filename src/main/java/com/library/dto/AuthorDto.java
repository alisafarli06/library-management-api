package com.library.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Author data transfer object")
public class AuthorDto {

	@Schema(description = "Author identifier", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
	private Long id;

	@NotBlank
	@Size(max = 255)
	@Schema(description = "Author full name", example = "Jane Austen")
	private String name;

	@Schema(
			description = "Number of books linked to this author",
			example = "3",
			accessMode = Schema.AccessMode.READ_ONLY)
	private Long bookCount;

	public AuthorDto() {
	}

	public AuthorDto(Long id, String name, Long bookCount) {
		this.id = id;
		this.name = name;
		this.bookCount = bookCount != null ? bookCount : 0L;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Long getBookCount() {
		return bookCount;
	}

	public void setBookCount(Long bookCount) {
		this.bookCount = bookCount;
	}
}
