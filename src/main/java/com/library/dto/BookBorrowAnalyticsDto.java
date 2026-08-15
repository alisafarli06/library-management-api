package com.library.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "How often a book has been borrowed, counting every Loan row")
public class BookBorrowAnalyticsDto {

	@Schema(description = "Book identifier", example = "9")
	private Long bookId;

	@Schema(description = "Book title", example = "Clean Code")
	private String bookTitle;

	@Schema(description = "Number of loan rows for this book", example = "12")
	private long borrowCount;

	public BookBorrowAnalyticsDto() {
	}

	public BookBorrowAnalyticsDto(Long bookId, String bookTitle, Long borrowCount) {
		this.bookId = bookId;
		this.bookTitle = bookTitle;
		this.borrowCount = borrowCount == null ? 0L : borrowCount;
	}

	public Long getBookId() {
		return bookId;
	}

	public void setBookId(Long bookId) {
		this.bookId = bookId;
	}

	public String getBookTitle() {
		return bookTitle;
	}

	public void setBookTitle(String bookTitle) {
		this.bookTitle = bookTitle;
	}

	public long getBorrowCount() {
		return borrowCount;
	}

	public void setBorrowCount(long borrowCount) {
		this.borrowCount = borrowCount;
	}
}
