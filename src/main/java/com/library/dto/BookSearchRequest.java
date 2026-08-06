package com.library.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public class BookSearchRequest {

	@Size(max = 255)
	private String title;

	@Size(max = 255)
	private String author;

	@Min(1000)
	@Max(9999)
	private Integer publishedAfter;

	@Min(1000)
	@Max(9999)
	private Integer yearFrom;

	@Min(1000)
	@Max(9999)
	private Integer yearTo;

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
