package com.library.controller;

import com.library.config.OpenApiConfig;
import com.library.config.openapi.RoleRestrictedResponses;
import com.library.dto.LoanDto;
import com.library.dto.UpdateProfileRequest;
import com.library.dto.UserProfileDto;
import com.library.exception.ErrorResponse;
import com.library.service.LoanService;
import com.library.service.MemberService;
import com.library.service.UserService;
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
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
@Tag(name = "User", description = "Endpoints accessible by users with USER or ADMIN role")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH_SCHEME)
@RoleRestrictedResponses
public class UserProfileController {

	private final UserService userService;
	private final MemberService memberService;
	private final LoanService loanService;

	public UserProfileController(UserService userService, MemberService memberService, LoanService loanService) {
		this.userService = userService;
		this.memberService = memberService;
		this.loanService = loanService;
	}

	@GetMapping("/profile")
	@Operation(
			summary = "Get authenticated user profile",
			description = "Returns the full name and email for the authenticated user. "
					+ "Identity comes from the JWT; the client cannot request another user's profile."
	)
	@ApiResponse(
			responseCode = "200",
			description = "Profile returned",
			content = @Content(
					mediaType = MediaType.APPLICATION_JSON_VALUE,
					schema = @Schema(implementation = UserProfileDto.class)))
	@ApiResponse(
			responseCode = "404",
			description = "Authenticated user not found",
			content = @Content(
					mediaType = MediaType.APPLICATION_JSON_VALUE,
					schema = @Schema(implementation = ErrorResponse.class)))
	public UserProfileDto profile(Authentication authentication) {
		return userService.getProfile(authentication.getName());
	}

	@PatchMapping("/profile")
	@Operation(
			summary = "Update authenticated user profile",
			description = "Updates the full name for the authenticated user. Email, role, and password cannot be changed here."
	)
	@ApiResponse(
			responseCode = "200",
			description = "Profile updated",
			content = @Content(
					mediaType = MediaType.APPLICATION_JSON_VALUE,
					schema = @Schema(implementation = UserProfileDto.class)))
	@ApiResponse(
			responseCode = "400",
			description = "Validation failed",
			content = @Content(
					mediaType = MediaType.APPLICATION_JSON_VALUE,
					schema = @Schema(implementation = ErrorResponse.class)))
	@ApiResponse(
			responseCode = "404",
			description = "Authenticated user not found",
			content = @Content(
					mediaType = MediaType.APPLICATION_JSON_VALUE,
					schema = @Schema(implementation = ErrorResponse.class)))
	public UserProfileDto updateProfile(
			@Valid @RequestBody UpdateProfileRequest request,
			Authentication authentication) {
		return userService.updateProfile(authentication.getName(), request.getName());
	}

	@PostMapping("/books/{bookId}/borrow")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(
			summary = "Borrow book for the authenticated user",
			description = "Borrows the book for the Member linked to the authenticated User. "
					+ "The member is resolved from the JWT identity; the client cannot supply another memberId."
	)
	@ApiResponse(responseCode = "204", description = "Book borrowed successfully")
	@ApiResponse(
			responseCode = "404",
			description = "Authenticated user, linked member, or book not found",
			content = @Content(
					mediaType = MediaType.APPLICATION_JSON_VALUE,
					schema = @Schema(implementation = ErrorResponse.class)))
	@ApiResponse(
			responseCode = "409",
			description = "Book is not available or member already borrowed this book",
			content = @Content(
					mediaType = MediaType.APPLICATION_JSON_VALUE,
					schema = @Schema(implementation = ErrorResponse.class)))
	public void borrowOwnBook(
			@Parameter(description = "Book identifier", required = true, example = "5") @PathVariable Long bookId,
			Authentication authentication) {
		memberService.borrowBookForAuthenticatedUser(authentication.getName(), bookId);
	}

	@PostMapping("/books/{bookId}/return")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(
			summary = "Return book for the authenticated user",
			description = "Returns the book for the Member linked to the authenticated User. "
					+ "The member is resolved from the JWT identity; the client cannot supply another memberId."
	)
	@ApiResponse(responseCode = "204", description = "Book returned successfully")
	@ApiResponse(
			responseCode = "404",
			description = "Authenticated user, linked member, book, or active loan not found",
			content = @Content(
					mediaType = MediaType.APPLICATION_JSON_VALUE,
					schema = @Schema(implementation = ErrorResponse.class)))
	public void returnOwnBook(
			@Parameter(description = "Book identifier", required = true, example = "5") @PathVariable Long bookId,
			Authentication authentication) {
		memberService.returnBookForAuthenticatedUser(authentication.getName(), bookId);
	}

	@GetMapping("/loans")
	@Operation(
			summary = "List loans for the authenticated user",
			description = "Returns active and historical loans for the Member linked to the JWT identity."
	)
	@ApiResponse(
			responseCode = "200",
			description = "Paginated list of the authenticated user's loans",
			content = @Content(
					mediaType = MediaType.APPLICATION_JSON_VALUE,
					schema = @Schema(implementation = LoanDto.class)))
	@ApiResponse(
			responseCode = "404",
			description = "Authenticated user or linked member not found",
			content = @Content(
					mediaType = MediaType.APPLICATION_JSON_VALUE,
					schema = @Schema(implementation = ErrorResponse.class)))
	public Page<LoanDto> ownLoans(
			@ParameterObject
			@PageableDefault(size = 20, sort = "borrowedAt", direction = Sort.Direction.DESC)
			Pageable pageable,
			Authentication authentication) {
		return loanService.findForAuthenticatedUser(authentication.getName(), pageable);
	}
}
