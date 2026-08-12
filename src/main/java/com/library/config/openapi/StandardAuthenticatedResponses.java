package com.library.config.openapi;

import com.library.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.MediaType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@ApiResponses({
		@ApiResponse(
				responseCode = "401",
				description = "Missing or invalid JWT access token",
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
public @interface StandardAuthenticatedResponses {
}
