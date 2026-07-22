package com.library.service;

import com.library.dto.BookDto;
import com.library.entity.Author;
import com.library.entity.Book;
import com.library.exception.ResourceNotFoundException;
import com.library.repository.AuthorRepository;
import com.library.repository.BookRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class BookService {

	private final BookRepository bookRepository;
	private final AuthorRepository authorRepository;

	public BookService(BookRepository bookRepository, AuthorRepository authorRepository) {
		this.bookRepository = bookRepository;
		this.authorRepository = authorRepository;
	}

	public Page<BookDto> findAll(Pageable pageable) {
		return bookRepository.findAll(pageable).map(this::toDto);
	}

	public BookDto findById(Long id) {
		Book book = bookRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + id));
		return toDto(book);
	}

	public BookDto create(BookDto bookDto) {
		Author author = findAuthor(bookDto.getAuthorId());
		Book book = toEntity(bookDto, author);
		book.setId(null);
		Book saved = bookRepository.save(book);
		return toDto(saved);
	}

	public BookDto update(Long id, BookDto bookDto) {
		Book book = bookRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + id));
		Author author = findAuthor(bookDto.getAuthorId());
		book.setTitle(bookDto.getTitle());
		book.setIsbn(bookDto.getIsbn());
		book.setPublishedYear(bookDto.getPublishedYear());
		book.setAuthor(author);
		Book saved = bookRepository.save(book);
		return toDto(saved);
	}

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

	private BookDto toDto(Book book) {
		BookDto dto = new BookDto();
		dto.setId(book.getId());
		dto.setTitle(book.getTitle());
		dto.setIsbn(book.getIsbn());
		dto.setPublishedYear(book.getPublishedYear());
		dto.setAuthorId(book.getAuthor().getId());
		return dto;
	}

	private Book toEntity(BookDto dto, Author author) {
		Book book = new Book();
		book.setId(dto.getId());
		book.setTitle(dto.getTitle());
		book.setIsbn(dto.getIsbn());
		book.setPublishedYear(dto.getPublishedYear());
		book.setAuthor(author);
		return book;
	}
}
