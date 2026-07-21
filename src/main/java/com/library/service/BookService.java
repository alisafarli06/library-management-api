package com.library.service;

import com.library.dto.BookDto;
import com.library.entity.Author;
import com.library.entity.Book;
import com.library.repository.AuthorRepository;
import com.library.repository.BookRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class BookService {

	private final BookRepository bookRepository;
	private final AuthorRepository authorRepository;

	public BookService(BookRepository bookRepository, AuthorRepository authorRepository) {
		this.bookRepository = bookRepository;
		this.authorRepository = authorRepository;
	}

	public List<BookDto> findAll() {
		return bookRepository.findAll().stream()
				.map(this::toDto)
				.toList();
	}

	public BookDto findById(Long id) {
		Book book = bookRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found"));
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
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found"));
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
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found");
		}
		bookRepository.deleteById(id);
	}

	private Author findAuthor(Long authorId) {
		return authorRepository.findById(authorId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Author not found"));
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
