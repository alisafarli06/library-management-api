package com.library.service;

import com.library.dto.AuthorDto;
import com.library.entity.Author;
import com.library.exception.ResourceNotFoundException;
import com.library.mapper.AuthorMapper;
import com.library.repository.AuthorRepository;
import com.library.repository.AuthorSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Transactional(readOnly = true)
public class AuthorService {

	private final AuthorRepository authorRepository;
	private final AuthorMapper authorMapper;

	public AuthorService(AuthorRepository authorRepository, AuthorMapper authorMapper) {
		this.authorRepository = authorRepository;
		this.authorMapper = authorMapper;
	}

	public Page<AuthorDto> findAll(Pageable pageable) {
		return search(null, pageable);
	}

	public Page<AuthorDto> search(String q, Pageable pageable) {
		String term = StringUtils.hasText(q) ? q.trim() : null;
		return authorRepository.findAll(AuthorSpecifications.nameContains(term), pageable).map(authorMapper::toDto);
	}

	public AuthorDto findById(Long id) {
		Author author = authorRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Author not found with id: " + id));
		return authorMapper.toDto(author);
	}

	@Transactional
	public AuthorDto create(AuthorDto authorDto) {
		Author author = authorMapper.toEntity(authorDto);
		author.setId(null);
		Author saved = authorRepository.save(author);
		AuthorDto dto = authorMapper.toDto(saved);
		if (dto.getBookCount() == null) {
			dto.setBookCount(0L);
		}
		return dto;
	}

	@Transactional
	public AuthorDto update(Long id, AuthorDto authorDto) {
		Author author = authorRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Author not found with id: " + id));
		author.setName(authorDto.getName());
		Author saved = authorRepository.save(author);
		return authorMapper.toDto(saved);
	}

	@Transactional
	public void delete(Long id) {
		if (!authorRepository.existsById(id)) {
			throw new ResourceNotFoundException("Author not found with id: " + id);
		}
		authorRepository.deleteById(id);
	}
}
