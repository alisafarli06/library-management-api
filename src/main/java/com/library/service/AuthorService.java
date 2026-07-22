package com.library.service;

import com.library.dto.AuthorDto;
import com.library.entity.Author;
import com.library.exception.ResourceNotFoundException;
import com.library.repository.AuthorRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class AuthorService {

	private final AuthorRepository authorRepository;

	public AuthorService(AuthorRepository authorRepository) {
		this.authorRepository = authorRepository;
	}

	public Page<AuthorDto> findAll(Pageable pageable) {
		return authorRepository.findAll(pageable).map(this::toDto);
	}

	public AuthorDto findById(Long id) {
		Author author = authorRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Author not found with id: " + id));
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
				.orElseThrow(() -> new ResourceNotFoundException("Author not found with id: " + id));
		author.setName(authorDto.getName());
		Author saved = authorRepository.save(author);
		return toDto(saved);
	}

	public void delete(Long id) {
		if (!authorRepository.existsById(id)) {
			throw new ResourceNotFoundException("Author not found with id: " + id);
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
