package com.library.controller;

import com.library.config.OpenApiConfig;
import com.library.config.openapi.StandardAuthenticatedResponses;
import com.library.dto.MemberDto;
import com.library.exception.ErrorResponse;
import com.library.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/members")
@Tag(name = "Members", description = "CRUD operations for members and book borrowing (JWT required)")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH_SCHEME)
@StandardAuthenticatedResponses
public class MemberController {

	private final MemberService memberService;

	public MemberController(MemberService memberService) {
		this.memberService = memberService;
	}

	@GetMapping
	@Operation(
			summary = "List members",
			description = "Returns a paginated, sortable list of members. "
					+ "Supports `page`, `size`, and `sort` query parameters (e.g. `sort=name,asc`)."
	)
	@ApiResponse(
			responseCode = "200",
			description = "Paginated list of members",
			content = @Content(
					mediaType = MediaType.APPLICATION_JSON_VALUE,
					schema = @Schema(implementation = MemberDto.class)))
	public Page<MemberDto> getAll(@ParameterObject Pageable pageable) {
		return memberService.findAll(pageable);
	}

	@GetMapping("/{id}")
	@Operation(summary = "Get member by ID", description = "Returns a single member by its identifier.")
	@ApiResponse(
			responseCode = "200",
			description = "Member found",
			content = @Content(
					mediaType = MediaType.APPLICATION_JSON_VALUE,
					schema = @Schema(implementation = MemberDto.class)))
	@ApiResponse(
			responseCode = "404",
			description = "Member not found",
			content = @Content(
					mediaType = MediaType.APPLICATION_JSON_VALUE,
					schema = @Schema(implementation = ErrorResponse.class)))
	public MemberDto getById(
			@Parameter(description = "Member identifier", required = true, example = "1") @PathVariable Long id) {
		return memberService.findById(id);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Create member", description = "Creates a new library member.")
	@ApiResponse(
			responseCode = "201",
			description = "Member created",
			content = @Content(
					mediaType = MediaType.APPLICATION_JSON_VALUE,
					schema = @Schema(implementation = MemberDto.class)))
	@ApiResponse(
			responseCode = "400",
			description = "Validation failed",
			content = @Content(
					mediaType = MediaType.APPLICATION_JSON_VALUE,
					schema = @Schema(implementation = ErrorResponse.class)))
	@ApiResponse(
			responseCode = "409",
			description = "Data integrity violation (e.g. duplicate email)",
			content = @Content(
					mediaType = MediaType.APPLICATION_JSON_VALUE,
					schema = @Schema(implementation = ErrorResponse.class)))
	public MemberDto create(@Valid @RequestBody MemberDto memberDto) {
		return memberService.create(memberDto);
	}

	@PutMapping("/{id}")
	@Operation(summary = "Update member", description = "Updates an existing member by its identifier.")
	@ApiResponse(
			responseCode = "200",
			description = "Member updated",
			content = @Content(
					mediaType = MediaType.APPLICATION_JSON_VALUE,
					schema = @Schema(implementation = MemberDto.class)))
	@ApiResponse(
			responseCode = "400",
			description = "Validation failed",
			content = @Content(
					mediaType = MediaType.APPLICATION_JSON_VALUE,
					schema = @Schema(implementation = ErrorResponse.class)))
	@ApiResponse(
			responseCode = "404",
			description = "Member not found",
			content = @Content(
					mediaType = MediaType.APPLICATION_JSON_VALUE,
					schema = @Schema(implementation = ErrorResponse.class)))
	@ApiResponse(
			responseCode = "409",
			description = "Data integrity violation",
			content = @Content(
					mediaType = MediaType.APPLICATION_JSON_VALUE,
					schema = @Schema(implementation = ErrorResponse.class)))
	public MemberDto update(
			@Parameter(description = "Member identifier", required = true, example = "1") @PathVariable Long id,
			@Valid @RequestBody MemberDto memberDto) {
		return memberService.update(id, memberDto);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Delete member", description = "Deletes a member by its identifier.")
	@ApiResponse(responseCode = "204", description = "Member deleted")
	@ApiResponse(
			responseCode = "404",
			description = "Member not found",
			content = @Content(
					mediaType = MediaType.APPLICATION_JSON_VALUE,
					schema = @Schema(implementation = ErrorResponse.class)))
	@ApiResponse(
			responseCode = "409",
			description = "Member cannot be deleted due to related borrow records",
			content = @Content(
					mediaType = MediaType.APPLICATION_JSON_VALUE,
					schema = @Schema(implementation = ErrorResponse.class)))
	public void delete(
			@Parameter(description = "Member identifier", required = true, example = "1") @PathVariable Long id) {
		memberService.delete(id);
	}

	@PostMapping("/{memberId}/books/{bookId}/borrow")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(
			summary = "Borrow book",
			description = "Lets a member borrow an available book in a single database transaction. "
					+ "The book must not already be borrowed by another member."
	)
	@ApiResponse(responseCode = "204", description = "Book borrowed successfully")
	@ApiResponse(
			responseCode = "404",
			description = "Member or book not found",
			content = @Content(
					mediaType = MediaType.APPLICATION_JSON_VALUE,
					schema = @Schema(implementation = ErrorResponse.class)))
	@ApiResponse(
			responseCode = "409",
			description = "Book is not available or member already borrowed this book",
			content = @Content(
					mediaType = MediaType.APPLICATION_JSON_VALUE,
					schema = @Schema(implementation = ErrorResponse.class)))
	public void borrowBook(
			@Parameter(description = "Member identifier", required = true, example = "1") @PathVariable Long memberId,
			@Parameter(description = "Book identifier", required = true, example = "5") @PathVariable Long bookId) {
		memberService.borrowBook(memberId, bookId);
	}

	@PostMapping("/{memberId}/books/{bookId}/return")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(
			summary = "Return book",
			description = "Marks the active loan as returned and makes the book available again."
	)
	@ApiResponse(responseCode = "204", description = "Book returned successfully")
	@ApiResponse(
			responseCode = "404",
			description = "Member, book, or active loan not found",
			content = @Content(
					mediaType = MediaType.APPLICATION_JSON_VALUE,
					schema = @Schema(implementation = ErrorResponse.class)))
	public void returnBook(
			@Parameter(description = "Member identifier", required = true, example = "1") @PathVariable Long memberId,
			@Parameter(description = "Book identifier", required = true, example = "5") @PathVariable Long bookId) {
		memberService.returnBook(memberId, bookId);
	}
}
