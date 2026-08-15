package com.library.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "How often a member has borrowed books, counting every Loan row")
public class MemberBorrowAnalyticsDto {

	@Schema(description = "Member identifier", example = "3")
	private Long memberId;

	@Schema(description = "Member name", example = "Ada Lovelace")
	private String memberName;

	@Schema(description = "Number of loan rows for this member", example = "8")
	private long borrowCount;

	public MemberBorrowAnalyticsDto() {
	}

	public MemberBorrowAnalyticsDto(Long memberId, String memberName, Long borrowCount) {
		this.memberId = memberId;
		this.memberName = memberName;
		this.borrowCount = borrowCount == null ? 0L : borrowCount;
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

	public long getBorrowCount() {
		return borrowCount;
	}

	public void setBorrowCount(long borrowCount) {
		this.borrowCount = borrowCount;
	}
}
