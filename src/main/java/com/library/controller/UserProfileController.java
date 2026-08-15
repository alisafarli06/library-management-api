package com.library.controller;

import com.library.config.OpenApiConfig;
import com.library.config.openapi.RoleRestrictedResponses;
import com.library.exception.ErrorResponse;
import com.library.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
@Tag(name = "User", description = "Endpoints accessible by users with USER or ADMIN role")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH_SCHEME)
@RoleRestrictedResponses
public class UserProfileController {

	private final MemberService memberService;

	public UserProfileController(MemberService memberService) {
		this.memberService = memberService;
	}

	@GetMapping("/profile")
	@Operation(
			summary = "User profile",
			description = "Sample user profile endpoint. Requires JWT with USER or ADMIN role."
	)
	@ApiResponse(
			responseCode = "200",
			description = "User content returned",
			content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE, schema = @Schema(type = "string", example = "User content")))
	public String profile() {
		return "User content";
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
}
