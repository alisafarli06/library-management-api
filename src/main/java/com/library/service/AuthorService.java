package com.library.service;

import com.library.dto.AuthorDto;
import com.library.entity.Author;
import com.library.exception.ResourceNotFoundException;
import com.library.mapper.AuthorMapper;
import com.library.repository.AuthorRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class AuthorService {

	private final AuthorRepository authorRepository;
	private final AuthorMapper authorMapper;

	public AuthorService(AuthorRepository authorRepository, AuthorMapper authorMapper) {
		this.authorRepository = authorRepository;
		this.authorMapper = authorMapper;
	}

	public Page<AuthorDto> findAll(Pageable pageable) {
		return authorRepository.findAll(pageable).map(authorMapper::toDto);
	}

	public AuthorDto findById(Long id) {
		Author author = authorRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Author not found with id: " + id));
		return authorMapper.toDto(author);
	}

	public AuthorDto create(AuthorDto authorDto) {
		Author author = authorMapper.toEntity(authorDto);
		author.setId(null);
		Author saved = authorRepository.save(author);
		return authorMapper.toDto(saved);
	}

	public AuthorDto update(Long id, AuthorDto authorDto) {
		Author author = authorRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Author not found with id: " + id));
		author.setName(authorDto.getName());
		Author saved = authorRepository.save(author);
		return authorMapper.toDto(saved);
	}

	public void delete(Long id) {
		if (!authorRepository.existsById(id)) {
			throw new ResourceNotFoundException("Author not found with id: " + id);
		}
		authorRepository.deleteById(id);
	}
}
