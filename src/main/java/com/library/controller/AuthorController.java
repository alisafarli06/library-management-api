package com.library.controller;

import com.library.dto.AuthorDto;
import com.library.service.AuthorService;
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
public class AuthorController {

	private final AuthorService authorService;

	public AuthorController(AuthorService authorService) {
		this.authorService = authorService;
	}

	@GetMapping
	public Page<AuthorDto> getAll(Pageable pageable) {
		return authorService.findAll(pageable);
	}

	@GetMapping("/{id}")
	public AuthorDto getById(@PathVariable Long id) {
		return authorService.findById(id);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public AuthorDto create(@Valid @RequestBody AuthorDto authorDto) {
		return authorService.create(authorDto);
	}

	@PutMapping("/{id}")
	public AuthorDto update(@PathVariable Long id, @Valid @RequestBody AuthorDto authorDto) {
		return authorService.update(id, authorDto);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Long id) {
		authorService.delete(id);
	}
}
