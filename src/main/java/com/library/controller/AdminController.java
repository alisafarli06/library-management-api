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
@RequestMapping("/api/admin")
@Tag(name = "Admin", description = "Endpoints accessible only by users with the ADMIN role")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH_SCHEME)
@RoleRestrictedResponses
public class AdminController {

	@GetMapping("/dashboard")
	@Operation(
			summary = "Admin dashboard",
			description = "Sample admin-only endpoint. Requires JWT with ADMIN role."
	)
	@ApiResponse(
			responseCode = "200",
			description = "Admin content returned",
			content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE, schema = @Schema(type = "string", example = "Admin content")))
	public String dashboard() {
		return "Admin content";
	}
}
