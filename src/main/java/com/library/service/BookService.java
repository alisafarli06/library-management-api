package com.library.service;

import com.library.dto.BookDto;
import com.library.dto.BookSearchRequest;
import com.library.entity.Author;
import com.library.entity.Book;
import com.library.exception.ResourceNotFoundException;
import com.library.mapper.BookMapper;
import com.library.repository.AuthorRepository;
import com.library.repository.BookRepository;
import com.library.repository.BookSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class BookService {

	private final BookRepository bookRepository;
	private final AuthorRepository authorRepository;
	private final BookMapper bookMapper;

	public BookService(BookRepository bookRepository, AuthorRepository authorRepository, BookMapper bookMapper) {
		this.bookRepository = bookRepository;
		this.authorRepository = authorRepository;
		this.bookMapper = bookMapper;
	}

	public Page<BookDto> findAll(Pageable pageable) {
		return bookRepository.findAll(pageable).map(bookMapper::toDto);
	}

	public Page<BookDto> search(BookSearchRequest request, Pageable pageable) {
		Specification<Book> specification = Specification
				.allOf(
						BookSpecifications.titleContains(request.getTitle()),
						BookSpecifications.authorNameContains(request.getAuthor()),
						BookSpecifications.publishedAfter(request.getPublishedAfter()),
						BookSpecifications.publishedYearFrom(request.getYearFrom()),
						BookSpecifications.publishedYearTo(request.getYearTo()),
						BookSpecifications.availability(request.getAvailable())
				);

		return bookRepository.findAll(specification, pageable).map(bookMapper::toDto);
	}

	public BookDto findById(Long id) {
		Book book = bookRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + id));
		return bookMapper.toDto(book);
	}

	@Transactional
	public BookDto create(BookDto bookDto) {
		Author author = findAuthor(bookDto.getAuthorId());
		Book book = bookMapper.toEntity(bookDto, author);
		book.setId(null);
		Book saved = bookRepository.save(book);
		return bookMapper.toDto(saved);
	}

	@Transactional
	public BookDto update(Long id, BookDto bookDto) {
		Book book = bookRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + id));
		Author author = findAuthor(bookDto.getAuthorId());
		book.setTitle(bookDto.getTitle());
		book.setIsbn(bookDto.getIsbn());
		book.setPublishedYear(bookDto.getPublishedYear());
		book.setAuthor(author);
		Book saved = bookRepository.save(book);
		return bookMapper.toDto(saved);
	}

	@Transactional
	public void delete(Long id) {
		if (!bookRepository.existsById(id)) {
			throw new ResourceNotFoundException("Book not found with id: " + id);
		}
		bookRepository.deleteById(id);
	}

	private Author findAuthor(Long authorId) {
		return authorRepository.findById(authorId)
				.orElseThrow(() -> new ResourceNotFoundException("Author not found with id: " + authorId));
	}
}
