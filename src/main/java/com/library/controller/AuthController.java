package com.library.controller;

import com.library.dto.AuthenticationResponse;
import com.library.dto.LoginRequest;
import com.library.dto.RefreshTokenRequest;
import com.library.dto.RegisterRequest;
import com.library.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "User registration and login")
public class AuthController {

	private final AuthenticationService authenticationService;

	public AuthController(AuthenticationService authenticationService) {
		this.authenticationService = authenticationService;
	}

	@PostMapping("/register")
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Register user", description = "Registers a new user and returns access and refresh tokens")
	public AuthenticationResponse register(@Valid @RequestBody RegisterRequest request) {
		return authenticationService.register(request);
	}

	@PostMapping("/login")
	@Operation(summary = "Login", description = "Authenticates a user and returns access and refresh tokens")
	public AuthenticationResponse login(@Valid @RequestBody LoginRequest request) {
		return authenticationService.login(request);
	}

	@PostMapping("/refresh")
	@Operation(summary = "Refresh tokens", description = "Validates a refresh token and returns a new token pair")
	public AuthenticationResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {
		return authenticationService.refresh(request);
	}
}
