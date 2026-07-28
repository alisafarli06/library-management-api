package com.library.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
@Tag(name = "User", description = "Endpoints accessible by USER and ADMIN")
public class UserProfileController {

	@GetMapping("/profile")
	@Operation(summary = "User profile", description = "Sample endpoint for USER and ADMIN roles")
	public String profile() {
		return "User content";
	}
}
