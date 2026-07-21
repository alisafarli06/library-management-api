package com.library.service;

import com.library.dto.AuthorDto;
import com.library.entity.Author;
import com.library.repository.AuthorRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class AuthorService {

	private final AuthorRepository authorRepository;

	public AuthorService(AuthorRepository authorRepository) {
		this.authorRepository = authorRepository;
	}

	public List<AuthorDto> findAll() {
		return authorRepository.findAll().stream()
				.map(this::toDto)
				.toList();
	}

	public AuthorDto findById(Long id) {
		Author author = authorRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Author not found"));
		return toDto(author);
	}

	public AuthorDto create(AuthorDto authorDto) {
		Author author = toEntity(authorDto);
		author.setId(null);
		Author saved = authorRepository.save(author);
		return toDto(saved);
	}

	public AuthorDto update(Long id, AuthorDto authorDto) {
		Author author = authorRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Author not found"));
		author.setName(authorDto.getName());
		Author saved = authorRepository.save(author);
		return toDto(saved);
	}

	public void delete(Long id) {
		if (!authorRepository.existsById(id)) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Author not found");
		}
		authorRepository.deleteById(id);
	}

	private AuthorDto toDto(Author author) {
		AuthorDto dto = new AuthorDto();
		dto.setId(author.getId());
		dto.setName(author.getName());
		return dto;
	}

	private Author toEntity(AuthorDto dto) {
		Author author = new Author();
		author.setId(dto.getId());
		author.setName(dto.getName());
		return author;
	}
}
