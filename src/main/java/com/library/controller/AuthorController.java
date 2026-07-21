package com.library.controller;

import com.library.dto.AuthorDto;
import com.library.service.AuthorService;
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

import java.util.List;

@RestController
@RequestMapping("/api/authors")
public class AuthorController {

	private final AuthorService authorService;

	public AuthorController(AuthorService authorService) {
		this.authorService = authorService;
	}

	@GetMapping
	public List<AuthorDto> getAll() {
		return authorService.findAll();
	}

	@GetMapping("/{id}")
	public AuthorDto getById(@PathVariable Long id) {
		return authorService.findById(id);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public AuthorDto create(@RequestBody AuthorDto authorDto) {
		return authorService.create(authorDto);
	}

	@PutMapping("/{id}")
	public AuthorDto update(@PathVariable Long id, @RequestBody AuthorDto authorDto) {
		return authorService.update(id, authorDto);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Long id) {
		authorService.delete(id);
	}
}
