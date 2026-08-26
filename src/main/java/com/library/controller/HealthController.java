package com.library.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Lightweight liveness probe for Render and local ops. Does not check the database.
 */
@RestController
@Tag(name = "Health", description = "Public liveness probe")
public class HealthController {

	@GetMapping(value = {"/api/health", "/health"}, produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "Liveness", description = "Returns UP when the HTTP process is running. Public; no JWT.")
	public Map<String, String> health() {
		return Map.of("status", "UP");
	}
}
