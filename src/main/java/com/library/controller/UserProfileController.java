package com.library.controller;

import com.library.config.OpenApiConfig;
import com.library.config.openapi.RoleRestrictedResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
@Tag(name = "User", description = "Endpoints accessible by users with USER or ADMIN role")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH_SCHEME)
@RoleRestrictedResponses
public class UserProfileController {

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
}
