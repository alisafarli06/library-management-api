package com.library.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Loan / borrowing record")
public class LoanDto {

	@Schema(description = "Loan identifier", example = "1")
	private Long id;

	@Schema(description = "Borrowing member identifier", example = "3")
	private Long memberId;

	@Schema(description = "Borrowing member name", example = "Ada Lovelace")
	private String memberName;

	@Schema(description = "Borrowed book identifier", example = "9")
	private Long bookId;

	@Schema(description = "Borrowed book title", example = "Clean Code")
	private String bookTitle;

	@Schema(description = "When the book was borrowed")
	private Instant borrowedAt;

	@Schema(description = "When the book was returned; null if still borrowed")
	private Instant returnedAt;

	public LoanDto() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getMemberId() {
		return memberId;
	}

	public void setMemberId(Long memberId) {
		this.memberId = memberId;
	}

	public String getMemberName() {
		return memberName;
	}

	public void setMemberName(String memberName) {
		this.memberName = memberName;
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

	public Instant getBorrowedAt() {
		return borrowedAt;
	}

	public void setBorrowedAt(Instant borrowedAt) {
		this.borrowedAt = borrowedAt;
	}

	public Instant getReturnedAt() {
		return returnedAt;
	}

	public void setReturnedAt(Instant returnedAt) {
		this.returnedAt = returnedAt;
	}
}
