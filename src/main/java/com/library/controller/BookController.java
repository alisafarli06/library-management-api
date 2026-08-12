package com.library.controller;

import com.library.config.OpenApiConfig;
import com.library.config.openapi.StandardAuthenticatedResponses;
import com.library.dto.BookDto;
import com.library.dto.BookSearchRequest;
import com.library.exception.ErrorResponse;
import com.library.service.BookService;
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
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/books")
@Tag(name = "Books", description = "CRUD operations and dynamic search for books (JWT required)")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH_SCHEME)
@StandardAuthenticatedResponses
public class BookController {

	private final BookService bookService;

	public BookController(BookService bookService) {
		this.bookService = bookService;
	}

	@GetMapping
	@Operation(
			summary = "List books",
			description = "Returns a paginated, sortable list of books. "
					+ "Supports `page`, `size`, and `sort` query parameters (e.g. `sort=title,asc`)."
	)
	@ApiResponse(
			responseCode = "200",
			description = "Paginated list of books",
			content = @Content(
					mediaType = MediaType.APPLICATION_JSON_VALUE,
					schema = @Schema(implementation = BookDto.class)))
	public Page<BookDto> getAll(@ParameterObject Pageable pageable) {
		return bookService.findAll(pageable);
	}

	@GetMapping("/search")
	@Operation(
			summary = "Search books",
			description = "Dynamically filters books by optional title, author name, publication year range, "
					+ "and availability. All filter query parameters are optional. "
					+ "Also supports pagination via `page`, `size`, and `sort`."
	)
	@ApiResponse(
			responseCode = "200",
			description = "Paginated search results",
			content = @Content(
					mediaType = MediaType.APPLICATION_JSON_VALUE,
					schema = @Schema(implementation = BookDto.class)))
	@ApiResponse(
			responseCode = "400",
			description = "Invalid search parameters (e.g. invalid year range)",
			content = @Content(
					mediaType = MediaType.APPLICATION_JSON_VALUE,
					schema = @Schema(implementation = ErrorResponse.class)))
	public Page<BookDto> search(
			@ParameterObject @Valid @ModelAttribute BookSearchRequest request,
			@ParameterObject Pageable pageable) {
		return bookService.search(request, pageable);
	}

	@GetMapping("/{id}")
	@Operation(summary = "Get book by ID", description = "Returns a single book by its identifier.")
	@ApiResponse(
			responseCode = "200",
			description = "Book found",
			content = @Content(
					mediaType = MediaType.APPLICATION_JSON_VALUE,
					schema = @Schema(implementation = BookDto.class)))
	@ApiResponse(
			responseCode = "404",
			description = "Book not found",
			content = @Content(
					mediaType = MediaType.APPLICATION_JSON_VALUE,
					schema = @Schema(implementation = ErrorResponse.class)))
	public BookDto getById(
			@Parameter(description = "Book identifier", required = true, example = "1") @PathVariable Long id) {
		return bookService.findById(id);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(
			summary = "Create book",
			description = "Creates a new book linked to an existing author (referenced by `authorId`)."
	)
	@ApiResponse(
			responseCode = "201",
			description = "Book created",
			content = @Content(
					mediaType = MediaType.APPLICATION_JSON_VALUE,
					schema = @Schema(implementation = BookDto.class)))
	@ApiResponse(
			responseCode = "400",
			description = "Validation failed",
			content = @Content(
					mediaType = MediaType.APPLICATION_JSON_VALUE,
					schema = @Schema(implementation = ErrorResponse.class)))
	@ApiResponse(
			responseCode = "404",
			description = "Referenced author not found",
			content = @Content(
					mediaType = MediaType.APPLICATION_JSON_VALUE,
					schema = @Schema(implementation = ErrorResponse.class)))
	@ApiResponse(
			responseCode = "409",
			description = "Data integrity violation (e.g. duplicate ISBN)",
			content = @Content(
					mediaType = MediaType.APPLICATION_JSON_VALUE,
					schema = @Schema(implementation = ErrorResponse.class)))
	public BookDto create(@Valid @RequestBody BookDto bookDto) {
		return bookService.create(bookDto);
	}

	@PutMapping("/{id}")
	@Operation(summary = "Update book", description = "Updates an existing book by its identifier.")
	@ApiResponse(
			responseCode = "200",
			description = "Book updated",
			content = @Content(
					mediaType = MediaType.APPLICATION_JSON_VALUE,
					schema = @Schema(implementation = BookDto.class)))
	@ApiResponse(
			responseCode = "400",
			description = "Validation failed",
			content = @Content(
					mediaType = MediaType.APPLICATION_JSON_VALUE,
					schema = @Schema(implementation = ErrorResponse.class)))
	@ApiResponse(
			responseCode = "404",
			description = "Book or referenced author not found",
			content = @Content(
					mediaType = MediaType.APPLICATION_JSON_VALUE,
					schema = @Schema(implementation = ErrorResponse.class)))
	@ApiResponse(
			responseCode = "409",
			description = "Data integrity violation",
			content = @Content(
					mediaType = MediaType.APPLICATION_JSON_VALUE,
					schema = @Schema(implementation = ErrorResponse.class)))
	public BookDto update(
			@Parameter(description = "Book identifier", required = true, example = "1") @PathVariable Long id,
			@Valid @RequestBody BookDto bookDto) {
		return bookService.update(id, bookDto);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Delete book", description = "Deletes a book by its identifier.")
	@ApiResponse(responseCode = "204", description = "Book deleted")
	@ApiResponse(
			responseCode = "404",
			description = "Book not found",
			content = @Content(
					mediaType = MediaType.APPLICATION_JSON_VALUE,
					schema = @Schema(implementation = ErrorResponse.class)))
	@ApiResponse(
			responseCode = "409",
			description = "Book cannot be deleted due to active borrow relationships",
			content = @Content(
					mediaType = MediaType.APPLICATION_JSON_VALUE,
					schema = @Schema(implementation = ErrorResponse.class)))
	public void delete(
			@Parameter(description = "Book identifier", required = true, example = "1") @PathVariable Long id) {
		bookService.delete(id);
	}
}
