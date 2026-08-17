package com.library.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

@Schema(description = "Optional filters for book search (all fields are optional)")
public class BookSearchRequest {

	@Size(max = 255)
	@Schema(description = "Case-insensitive partial match on book title", example = "Pride")
	private String title;

	@Size(max = 255)
	@Schema(description = "Case-insensitive partial match on author name", example = "Austen")
	private String author;

	@Positive
	@Schema(description = "Exact match on author identifier (takes precedence over author name)", example = "1")
	private Long authorId;

	@Min(1000)
	@Max(9999)
	@Schema(description = "Return books published after this year (inclusive)", example = "1800")
	private Integer publishedAfter;

	@Min(1000)
	@Max(9999)
	@Schema(description = "Minimum publication year (inclusive)", example = "1800")
	private Integer yearFrom;

	@Min(1000)
	@Max(9999)
	@Schema(description = "Maximum publication year (inclusive)", example = "1900")
	private Integer yearTo;

	@Schema(description = "When true, only available books; when false, only borrowed books", example = "true")
	private Boolean available;

	public BookSearchRequest() {
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public Long getAuthorId() {
		return authorId;
	}

	public void setAuthorId(Long authorId) {
		this.authorId = authorId;
	}

	public Integer getPublishedAfter() {
		return publishedAfter;
	}

	public void setPublishedAfter(Integer publishedAfter) {
		this.publishedAfter = publishedAfter;
	}

	public Integer getYearFrom() {
		return yearFrom;
	}

	public void setYearFrom(Integer yearFrom) {
		this.yearFrom = yearFrom;
	}

	public Integer getYearTo() {
		return yearTo;
	}

	public void setYearTo(Integer yearTo) {
		this.yearTo = yearTo;
	}

	public Boolean getAvailable() {
		return available;
	}

	public void setAvailable(Boolean available) {
		this.available = available;
	}

	@AssertTrue(message = "yearFrom must be less than or equal to yearTo")
	public boolean isYearRangeValid() {
		if (yearFrom == null || yearTo == null) {
			return true;
		}
		return yearFrom <= yearTo;
	}
}
