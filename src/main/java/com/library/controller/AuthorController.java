package com.library.controller;

import com.library.dto.AuthorDto;
import com.library.service.AuthorService;
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
@RequestMapping("/api/authors")
@Tag(name = "Authors", description = "CRUD operations for authors")
public class AuthorController {

	private final AuthorService authorService;

	public AuthorController(AuthorService authorService) {
		this.authorService = authorService;
	}

	@GetMapping
	@Operation(summary = "List authors", description = "Returns a paginated and sortable list of authors")
	public Page<AuthorDto> getAll(Pageable pageable) {
		return authorService.findAll(pageable);
	}

	@GetMapping("/{id}")
	@Operation(summary = "Get author by ID", description = "Returns a single author by its identifier")
	public AuthorDto getById(@PathVariable Long id) {
		return authorService.findById(id);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Create author", description = "Creates a new author")
	public AuthorDto create(@Valid @RequestBody AuthorDto authorDto) {
		return authorService.create(authorDto);
	}

	@PutMapping("/{id}")
	@Operation(summary = "Update author", description = "Updates an existing author by its identifier")
	public AuthorDto update(@PathVariable Long id, @Valid @RequestBody AuthorDto authorDto) {
		return authorService.update(id, authorDto);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Delete author", description = "Deletes an author by its identifier")
	public void delete(@PathVariable Long id) {
		authorService.delete(id);
	}
}
