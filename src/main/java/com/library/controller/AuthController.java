package com.library.controller;

import com.library.dto.AuthenticationResponse;
import com.library.dto.LoginRequest;
import com.library.dto.RefreshTokenRequest;
import com.library.dto.RegisterRequest;
import com.library.exception.ErrorResponse;
import com.library.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "User registration, login, and token refresh (public — no JWT required)")
public class AuthController {

	private final AuthenticationService authenticationService;

	public AuthController(AuthenticationService authenticationService) {
		this.authenticationService = authenticationService;
	}

	@PostMapping("/register")
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(
			summary = "Register user",
			description = "Creates a new user account and returns JWT access and refresh tokens. "
					+ "Does not require authentication."
	)
	@ApiResponses({
			@ApiResponse(
					responseCode = "201",
					description = "User registered successfully",
					content = @Content(
							mediaType = MediaType.APPLICATION_JSON_VALUE,
							schema = @Schema(implementation = AuthenticationResponse.class))),
			@ApiResponse(
					responseCode = "400",
					description = "Validation failed (invalid email, password length, etc.)",
					content = @Content(
							mediaType = MediaType.APPLICATION_JSON_VALUE,
							schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(
					responseCode = "409",
					description = "Email already registered",
					content = @Content(
							mediaType = MediaType.APPLICATION_JSON_VALUE,
							schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(
					responseCode = "500",
					description = "Unexpected server error",
					content = @Content(
							mediaType = MediaType.APPLICATION_JSON_VALUE,
							schema = @Schema(implementation = ErrorResponse.class)))
	})
	public AuthenticationResponse register(@Valid @RequestBody RegisterRequest request) {
		return authenticationService.register(request);
	}

	@PostMapping("/login")
	@Operation(
			summary = "Login",
			description = "Authenticates with email and password and returns JWT access and refresh tokens. "
					+ "Does not require authentication."
	)
	@ApiResponses({
			@ApiResponse(
					responseCode = "200",
					description = "Login successful",
					content = @Content(
							mediaType = MediaType.APPLICATION_JSON_VALUE,
							schema = @Schema(implementation = AuthenticationResponse.class))),
			@ApiResponse(
					responseCode = "400",
					description = "Validation failed",
					content = @Content(
							mediaType = MediaType.APPLICATION_JSON_VALUE,
							schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(
					responseCode = "401",
					description = "Invalid email or password",
					content = @Content(
							mediaType = MediaType.APPLICATION_JSON_VALUE,
							schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(
					responseCode = "500",
					description = "Unexpected server error",
					content = @Content(
							mediaType = MediaType.APPLICATION_JSON_VALUE,
							schema = @Schema(implementation = ErrorResponse.class)))
	})
	public AuthenticationResponse login(@Valid @RequestBody LoginRequest request) {
		return authenticationService.login(request);
	}

	@PostMapping("/refresh")
	@Operation(
			summary = "Refresh tokens",
			description = "Validates a refresh token and returns a new access and refresh token pair. "
					+ "Does not require authentication."
	)
	@ApiResponses({
			@ApiResponse(
					responseCode = "200",
					description = "Tokens refreshed successfully",
					content = @Content(
							mediaType = MediaType.APPLICATION_JSON_VALUE,
							schema = @Schema(implementation = AuthenticationResponse.class))),
			@ApiResponse(
					responseCode = "400",
					description = "Validation failed (missing refresh token)",
					content = @Content(
							mediaType = MediaType.APPLICATION_JSON_VALUE,
							schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(
					responseCode = "401",
					description = "Invalid or expired refresh token",
					content = @Content(
							mediaType = MediaType.APPLICATION_JSON_VALUE,
							schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(
					responseCode = "500",
					description = "Unexpected server error",
					content = @Content(
							mediaType = MediaType.APPLICATION_JSON_VALUE,
							schema = @Schema(implementation = ErrorResponse.class)))
	})
	public AuthenticationResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {
		return authenticationService.refresh(request);
	}
}
