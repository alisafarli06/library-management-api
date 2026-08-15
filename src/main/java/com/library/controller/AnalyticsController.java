package com.library.controller;

import com.library.config.OpenApiConfig;
import com.library.config.openapi.RoleRestrictedResponses;
import com.library.dto.AuthorBorrowAnalyticsDto;
import com.library.dto.BookBorrowAnalyticsDto;
import com.library.dto.LoanAnalyticsSummaryDto;
import com.library.dto.MemberBorrowAnalyticsDto;
import com.library.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/analytics")
@Tag(name = "Analytics", description = "Borrowing analytics from Loan history (ADMIN)")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH_SCHEME)
@RoleRestrictedResponses
public class AnalyticsController {

	private final AnalyticsService analyticsService;

	public AnalyticsController(AnalyticsService analyticsService) {
		this.analyticsService = analyticsService;
	}

	@GetMapping("/summary")
	@Operation(
			summary = "Loan analytics summary",
			description = "ADMIN-only aggregates over all Loan rows: total, active (`returnedAt` is null), "
					+ "returned, distinct books borrowed, and distinct members with loans. "
					+ "Does not use `member_books`. Empty history returns zeros, not 404."
	)
	@ApiResponse(
			responseCode = "200",
			description = "Summary totals",
			content = @Content(
					mediaType = MediaType.APPLICATION_JSON_VALUE,
					schema = @Schema(implementation = LoanAnalyticsSummaryDto.class)))
	public LoanAnalyticsSummaryDto getSummary() {
		return analyticsService.getSummary();
	}

	@GetMapping("/books")
	@Operation(
			summary = "Most borrowed books",
			description = "ADMIN-only. Counts every Loan row per book (repeat borrows after return increase the count). "
					+ "Ordered by borrowCount descending, then bookId ascending. "
					+ "Supports `page` and `size` (default size 10)."
	)
	@ApiResponse(
			responseCode = "200",
			description = "Paginated book borrow counts",
			content = @Content(
					mediaType = MediaType.APPLICATION_JSON_VALUE,
					schema = @Schema(implementation = BookBorrowAnalyticsDto.class)))
	public Page<BookBorrowAnalyticsDto> getMostBorrowedBooks(
			@ParameterObject
			@PageableDefault(size = 10)
			Pageable pageable) {
		return analyticsService.getMostBorrowedBooks(pageable);
	}

	@GetMapping("/authors")
	@Operation(
			summary = "Most borrowed authors",
			description = "ADMIN-only. An author's borrow count is the number of Loan rows for that author's books "
					+ "(not distinct books). Ordered by borrowCount descending, then authorId ascending. "
					+ "Supports `page` and `size` (default size 10)."
	)
	@ApiResponse(
			responseCode = "200",
			description = "Paginated author borrow counts",
			content = @Content(
					mediaType = MediaType.APPLICATION_JSON_VALUE,
					schema = @Schema(implementation = AuthorBorrowAnalyticsDto.class)))
	public Page<AuthorBorrowAnalyticsDto> getMostBorrowedAuthors(
			@ParameterObject
			@PageableDefault(size = 10)
			Pageable pageable) {
		return analyticsService.getMostBorrowedAuthors(pageable);
	}

	@GetMapping("/members")
	@Operation(
			summary = "Most active members",
			description = "ADMIN-only. Counts Loan rows per member. Does not expose email or user identifiers. "
					+ "Ordered by borrowCount descending, then memberId ascending. "
					+ "Supports `page` and `size` (default size 10)."
	)
	@ApiResponse(
			responseCode = "200",
			description = "Paginated member borrow counts",
			content = @Content(
					mediaType = MediaType.APPLICATION_JSON_VALUE,
					schema = @Schema(implementation = MemberBorrowAnalyticsDto.class)))
	public Page<MemberBorrowAnalyticsDto> getMostActiveMembers(
			@ParameterObject
			@PageableDefault(size = 10)
			Pageable pageable) {
		return analyticsService.getMostActiveMembers(pageable);
	}
}
