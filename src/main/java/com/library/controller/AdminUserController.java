package com.library.controller;

import com.library.config.OpenApiConfig;
import com.library.config.openapi.RoleRestrictedResponses;
import com.library.dto.AdminUserDto;
import com.library.dto.UpdateUserRoleRequest;
import com.library.entity.Role;
import com.library.exception.ErrorResponse;
import com.library.service.AdminUserService;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Users", description = "ADMIN-only account management")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH_SCHEME)
@RoleRestrictedResponses
public class AdminUserController {

	private final AdminUserService adminUserService;

	public AdminUserController(AdminUserService adminUserService) {
		this.adminUserService = adminUserService;
	}

	@GetMapping
	@Operation(
			summary = "List users",
			description = "Returns a paginated list of user accounts. Optional `q` matches full name or email "
					+ "(case-insensitive contains). Optional `role` filters by USER or ADMIN. "
					+ "Password hashes are never returned."
	)
	@ApiResponse(
			responseCode = "200",
			description = "Paginated list of users",
			content = @Content(
					mediaType = MediaType.APPLICATION_JSON_VALUE,
					schema = @Schema(implementation = AdminUserDto.class)))
	public Page<AdminUserDto> search(
			@RequestParam(required = false) String q,
			@RequestParam(required = false) Role role,
			@ParameterObject Pageable pageable) {
		return adminUserService.search(q, role, pageable);
	}

	@GetMapping("/{id}")
	@Operation(summary = "Get user by ID", description = "Returns a single user account. Password hashes are never returned.")
	@ApiResponse(
			responseCode = "200",
			description = "User found",
			content = @Content(
					mediaType = MediaType.APPLICATION_JSON_VALUE,
					schema = @Schema(implementation = AdminUserDto.class)))
	@ApiResponse(
			responseCode = "404",
			description = "User not found",
			content = @Content(
					mediaType = MediaType.APPLICATION_JSON_VALUE,
					schema = @Schema(implementation = ErrorResponse.class)))
	public AdminUserDto getById(
			@Parameter(description = "User identifier", required = true, example = "1") @PathVariable Long id) {
		return adminUserService.findById(id);
	}

	@PatchMapping("/{id}/role")
	@Operation(
			summary = "Change user role",
			description = "Sets a user's role to USER or ADMIN. Rejects removing ADMIN from the last remaining admin."
	)
	@ApiResponse(
			responseCode = "200",
			description = "Role updated",
			content = @Content(
					mediaType = MediaType.APPLICATION_JSON_VALUE,
					schema = @Schema(implementation = AdminUserDto.class)))
	@ApiResponse(
			responseCode = "400",
			description = "Validation failed",
			content = @Content(
					mediaType = MediaType.APPLICATION_JSON_VALUE,
					schema = @Schema(implementation = ErrorResponse.class)))
	@ApiResponse(
			responseCode = "404",
			description = "User not found",
			content = @Content(
					mediaType = MediaType.APPLICATION_JSON_VALUE,
					schema = @Schema(implementation = ErrorResponse.class)))
	@ApiResponse(
			responseCode = "409",
			description = "Last remaining ADMIN cannot be downgraded",
			content = @Content(
					mediaType = MediaType.APPLICATION_JSON_VALUE,
					schema = @Schema(implementation = ErrorResponse.class)))
	public AdminUserDto updateRole(
			@Parameter(description = "User identifier", required = true, example = "1") @PathVariable Long id,
			@Valid @RequestBody UpdateUserRoleRequest request) {
		return adminUserService.updateRole(id, request.getRole());
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(
			summary = "Delete user",
			description = "Deletes a user account and the linked member when that member has no borrow records. "
					+ "Rejects deleting your own account or the last remaining ADMIN."
	)
	@ApiResponse(responseCode = "204", description = "User deleted")
	@ApiResponse(
			responseCode = "404",
			description = "User not found",
			content = @Content(
					mediaType = MediaType.APPLICATION_JSON_VALUE,
					schema = @Schema(implementation = ErrorResponse.class)))
	@ApiResponse(
			responseCode = "409",
			description = "User cannot be deleted (own account, last ADMIN, or linked borrow records)",
			content = @Content(
					mediaType = MediaType.APPLICATION_JSON_VALUE,
					schema = @Schema(implementation = ErrorResponse.class)))
	public void delete(
			@Parameter(description = "User identifier", required = true, example = "1") @PathVariable Long id,
			Authentication authentication) {
		adminUserService.delete(id, authentication.getName());
	}
}
