package com.library.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Aggregate borrowing statistics derived from Loan history")
public class LoanAnalyticsSummaryDto {

	@Schema(description = "Total number of loan rows", example = "42")
	private long totalLoans;

	@Schema(description = "Loans that have not been returned (returnedAt is null)", example = "7")
	private long activeLoans;

	@Schema(description = "Loans that have been returned (returnedAt is not null)", example = "35")
	private long returnedLoans;

	@Schema(description = "Distinct books that appear in at least one loan", example = "18")
	private long totalBooksBorrowed;

	@Schema(description = "Distinct members that appear in at least one loan", example = "12")
	private long totalMembersWithLoans;

	public LoanAnalyticsSummaryDto() {
	}

	public LoanAnalyticsSummaryDto(
			long totalLoans,
			long activeLoans,
			long returnedLoans,
			long totalBooksBorrowed,
			long totalMembersWithLoans) {
		this.totalLoans = totalLoans;
		this.activeLoans = activeLoans;
		this.returnedLoans = returnedLoans;
		this.totalBooksBorrowed = totalBooksBorrowed;
		this.totalMembersWithLoans = totalMembersWithLoans;
	}

	public long getTotalLoans() {
		return totalLoans;
	}

	public void setTotalLoans(long totalLoans) {
		this.totalLoans = totalLoans;
	}

	public long getActiveLoans() {
		return activeLoans;
	}

	public void setActiveLoans(long activeLoans) {
		this.activeLoans = activeLoans;
	}

	public long getReturnedLoans() {
		return returnedLoans;
	}

	public void setReturnedLoans(long returnedLoans) {
		this.returnedLoans = returnedLoans;
	}

	public long getTotalBooksBorrowed() {
		return totalBooksBorrowed;
	}

	public void setTotalBooksBorrowed(long totalBooksBorrowed) {
		this.totalBooksBorrowed = totalBooksBorrowed;
	}

	public long getTotalMembersWithLoans() {
		return totalMembersWithLoans;
	}

	public void setTotalMembersWithLoans(long totalMembersWithLoans) {
		this.totalMembersWithLoans = totalMembersWithLoans;
	}
}
