package com.library.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin", description = "Endpoints accessible only by ADMIN")
public class AdminController {

	@GetMapping("/dashboard")
	@Operation(summary = "Admin dashboard", description = "Sample endpoint for ADMIN role only")
	public String dashboard() {
		return "Admin content";
	}
}
