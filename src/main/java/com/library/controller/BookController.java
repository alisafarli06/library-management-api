package com.library.controller;

import com.library.dto.BookDto;
import com.library.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/books")
@Tag(name = "Books", description = "CRUD operations for books")
public class BookController {

	private final BookService bookService;

	public BookController(BookService bookService) {
		this.bookService = bookService;
	}

	@GetMapping
	@Operation(summary = "List books", description = "Returns a paginated and sortable list of books")
	public Page<BookDto> getAll(Pageable pageable) {
		return bookService.findAll(pageable);
	}

	@GetMapping("/{id}")
	@Operation(summary = "Get book by ID", description = "Returns a single book by its identifier")
	public BookDto getById(@PathVariable Long id) {
		return bookService.findById(id);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Create book", description = "Creates a new book linked to an existing author")
	public BookDto create(@Valid @RequestBody BookDto bookDto) {
		return bookService.create(bookDto);
	}

	@PutMapping("/{id}")
	@Operation(summary = "Update book", description = "Updates an existing book by its identifier")
	public BookDto update(@PathVariable Long id, @Valid @RequestBody BookDto bookDto) {
		return bookService.update(id, bookDto);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Delete book", description = "Deletes a book by its identifier")
	public void delete(@PathVariable Long id) {
		bookService.delete(id);
	}
}
