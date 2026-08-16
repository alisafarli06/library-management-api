package com.library.controller;

import com.library.config.OpenApiConfig;
import com.library.config.openapi.StandardAuthenticatedResponses;
import com.library.dto.AuthorDto;
import com.library.exception.ErrorResponse;
import com.library.service.AuthorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/authors")
@Tag(name = "Authors", description = "CRUD operations for authors (JWT required)")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH_SCHEME)
@StandardAuthenticatedResponses
public class AuthorController {

	private final AuthorService authorService;

	public AuthorController(AuthorService authorService) {
		this.authorService = authorService;
	}

	@GetMapping
	@Operation(
			summary = "List authors",
			description = "Returns a paginated, sortable list of authors. "
					+ "Supports `page`, `size`, and `sort` query parameters (e.g. `sort=name,asc`). "
					+ "Each result includes `bookCount` for linked books."
	)
	@ApiResponse(
			responseCode = "200",
			description = "Paginated list of authors",
			content = @Content(
					mediaType = MediaType.APPLICATION_JSON_VALUE,
					schema = @Schema(implementation = AuthorDto.class)))
	public Page<AuthorDto> getAll(@ParameterObject Pageable pageable) {
		return authorService.findAll(pageable);
	}

	@GetMapping("/search")
	@Operation(
			summary = "Search authors",
			description = "Returns a paginated list of authors filtered by optional `q` (case-insensitive name contains). "
					+ "Supports `page`, `size`, and `sort` query parameters (e.g. `sort=name,asc`). "
					+ "Each result includes `bookCount` for linked books.")
	@ApiResponse(
			responseCode = "200",
			description = "Paginated search results",
			content = @Content(
					mediaType = MediaType.APPLICATION_JSON_VALUE,
					schema = @Schema(implementation = AuthorDto.class)))
	public Page<AuthorDto> search(
			@RequestParam(required = false) String q,
			@ParameterObject Pageable pageable) {
		return authorService.search(q, pageable);
	}

	@GetMapping("/{id}")
	@Operation(summary = "Get author by ID", description = "Returns a single author by its identifier.")
	@ApiResponse(
			responseCode = "200",
			description = "Author found",
			content = @Content(
					mediaType = MediaType.APPLICATION_JSON_VALUE,
					schema = @Schema(implementation = AuthorDto.class)))
	@ApiResponse(
			responseCode = "404",
			description = "Author not found",
			content = @Content(
					mediaType = MediaType.APPLICATION_JSON_VALUE,
					schema = @Schema(implementation = ErrorResponse.class)))
	public AuthorDto getById(
			@Parameter(description = "Author identifier", required = true, example = "1") @PathVariable Long id) {
		return authorService.findById(id);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Create author", description = "Creates a new author record.")
	@ApiResponse(
			responseCode = "201",
			description = "Author created",
			content = @Content(
					mediaType = MediaType.APPLICATION_JSON_VALUE,
					schema = @Schema(implementation = AuthorDto.class)))
	@ApiResponse(
			responseCode = "400",
			description = "Validation failed",
			content = @Content(
					mediaType = MediaType.APPLICATION_JSON_VALUE,
					schema = @Schema(implementation = ErrorResponse.class)))
	@ApiResponse(
			responseCode = "409",
			description = "Data integrity violation (e.g. duplicate constraint)",
			content = @Content(
					mediaType = MediaType.APPLICATION_JSON_VALUE,
					schema = @Schema(implementation = ErrorResponse.class)))
	public AuthorDto create(@Valid @RequestBody AuthorDto authorDto) {
		return authorService.create(authorDto);
	}

	@PutMapping("/{id}")
	@Operation(summary = "Update author", description = "Updates an existing author by its identifier.")
	@ApiResponse(
			responseCode = "200",
			description = "Author updated",
			content = @Content(
					mediaType = MediaType.APPLICATION_JSON_VALUE,
					schema = @Schema(implementation = AuthorDto.class)))
	@ApiResponse(
			responseCode = "400",
			description = "Validation failed",
			content = @Content(
					mediaType = MediaType.APPLICATION_JSON_VALUE,
					schema = @Schema(implementation = ErrorResponse.class)))
	@ApiResponse(
			responseCode = "404",
			description = "Author not found",
			content = @Content(
					mediaType = MediaType.APPLICATION_JSON_VALUE,
					schema = @Schema(implementation = ErrorResponse.class)))
	@ApiResponse(
			responseCode = "409",
			description = "Data integrity violation",
			content = @Content(
					mediaType = MediaType.APPLICATION_JSON_VALUE,
					schema = @Schema(implementation = ErrorResponse.class)))
	public AuthorDto update(
			@Parameter(description = "Author identifier", required = true, example = "1") @PathVariable Long id,
			@Valid @RequestBody AuthorDto authorDto) {
		return authorService.update(id, authorDto);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Delete author", description = "Deletes an author by its identifier.")
	@ApiResponse(responseCode = "204", description = "Author deleted")
	@ApiResponse(
			responseCode = "404",
			description = "Author not found",
			content = @Content(
					mediaType = MediaType.APPLICATION_JSON_VALUE,
					schema = @Schema(implementation = ErrorResponse.class)))
	@ApiResponse(
			responseCode = "409",
			description = "Author cannot be deleted due to related books",
			content = @Content(
					mediaType = MediaType.APPLICATION_JSON_VALUE,
					schema = @Schema(implementation = ErrorResponse.class)))
	public void delete(
			@Parameter(description = "Author identifier", required = true, example = "1") @PathVariable Long id) {
		authorService.delete(id);
	}
}
