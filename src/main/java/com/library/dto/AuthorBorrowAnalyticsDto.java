package com.library.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "How often books by an author have been borrowed, counting every Loan row")
public class AuthorBorrowAnalyticsDto {

	@Schema(description = "Author identifier", example = "4")
	private Long authorId;

	@Schema(description = "Author name", example = "Robert C. Martin")
	private String authorName;

	@Schema(description = "Number of loan rows for books belonging to this author", example = "20")
	private long borrowCount;

	public AuthorBorrowAnalyticsDto() {
	}

	public AuthorBorrowAnalyticsDto(Long authorId, String authorName, Long borrowCount) {
		this.authorId = authorId;
		this.authorName = authorName;
		this.borrowCount = borrowCount == null ? 0L : borrowCount;
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

	public long getBorrowCount() {
		return borrowCount;
	}

	public void setBorrowCount(long borrowCount) {
		this.borrowCount = borrowCount;
	}
}
