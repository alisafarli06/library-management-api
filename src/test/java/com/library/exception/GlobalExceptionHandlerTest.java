package com.library.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class GlobalExceptionHandlerTest {

	private GlobalExceptionHandler globalExceptionHandler;

	@BeforeEach
	void setUp() {
		globalExceptionHandler = new GlobalExceptionHandler();
	}

	@Test
	void handleResourceNotFound_returnsNotFoundErrorResponse() {
		// Arrange
		ResourceNotFoundException exception = new ResourceNotFoundException("Author not found with id: 1");

		// Act
		ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleResourceNotFound(exception);

		// Assert
		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
		ErrorResponse body = response.getBody();
		assertNotNull(body);
		assertEquals(404, body.getStatus());
		assertEquals("Not Found", body.getError());
		assertEquals("Author not found with id: 1", body.getMessage());
		assertNotNull(body.getTimestamp());
		assertNull(body.getFieldErrors());
	}

	@Test
	void handleValidationErrors_returnsBadRequestWithFieldErrors() {
		// Arrange
		BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "authorDto");
		bindingResult.addError(new FieldError("authorDto", "name", "must not be blank"));
		MethodArgumentNotValidException exception = new MethodArgumentNotValidException(null, bindingResult);

		// Act
		ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleValidationErrors(exception);

		// Assert
		assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
		ErrorResponse body = response.getBody();
		assertNotNull(body);
		assertEquals(400, body.getStatus());
		assertEquals("Bad Request", body.getError());
		assertEquals("Validation failed", body.getMessage());
		assertNotNull(body.getFieldErrors());
		assertEquals("must not be blank", body.getFieldErrors().get("name"));
		assertNotNull(body.getTimestamp());
	}

	@Test
	void handleUnexpectedError_returnsInternalServerErrorWithoutExposingDetails() {
		// Arrange
		Exception exception = new RuntimeException("sensitive database details");

		// Act
		ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleUnexpectedError(exception);

		// Assert
		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
		ErrorResponse body = response.getBody();
		assertNotNull(body);
		assertEquals(500, body.getStatus());
		assertEquals("Internal Server Error", body.getError());
		assertEquals("An unexpected error occurred", body.getMessage());
		assertNotNull(body.getTimestamp());
		assertNull(body.getFieldErrors());
	}
}
