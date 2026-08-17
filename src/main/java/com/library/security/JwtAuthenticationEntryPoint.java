package com.library.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.library.exception.ErrorResponse;
import com.library.service.AdminUserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

	public static final String JWT_ERROR_ATTRIBUTE = "jwt.error";

	private final ObjectMapper objectMapper;

	public JwtAuthenticationEntryPoint() {
		this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
	}

	@Override
	public void commence(
			HttpServletRequest request,
			HttpServletResponse response,
			AuthenticationException authException) throws IOException {
		String message = "Unauthorized";
		HttpStatus status = HttpStatus.UNAUTHORIZED;
		String error = "Unauthorized";
		Object jwtError = request.getAttribute(JWT_ERROR_ATTRIBUTE);
		if (jwtError instanceof String customMessage && !customMessage.isBlank()) {
			message = customMessage;
			if (AdminUserService.ACCOUNT_BLOCKED_MESSAGE.equals(customMessage)) {
				status = HttpStatus.FORBIDDEN;
				error = "Forbidden";
			}
		}

		ErrorResponse body = new ErrorResponse(
				Instant.now(),
				status.value(),
				error,
				message
		);

		response.setStatus(status.value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		objectMapper.writeValue(response.getOutputStream(), body);
	}
}
