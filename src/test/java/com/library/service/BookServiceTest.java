package com.library.service;

import com.library.dto.BookDto;
import com.library.entity.Author;
import com.library.entity.Book;
import com.library.exception.ResourceNotFoundException;
import com.library.mapper.BookMapper;
import com.library.repository.AuthorRepository;
import com.library.repository.BookRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

	@Mock
	private BookRepository bookRepository;

	@Mock
	private AuthorRepository authorRepository;

	@Spy
	private BookMapper bookMapper = new BookMapper();

	@InjectMocks
	private BookService bookService;

	@Test
	void findAll_returnsMappedBookDtos() {
		// Arrange
		Author author = createAuthor(1L, "Ali Safarli");
		Book book = createBook(1L, "Harry Potter and the Philosopher's Stone", "9780747532699", 1997, author);
		Pageable pageable = PageRequest.of(0, 10);
		when(bookRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(book)));

		// Act
		Page<BookDto> result = bookService.findAll(pageable);

		// Assert
		assertEquals(1, result.getTotalElements());
		BookDto dto = result.getContent().getFirst();
		assertEquals(1L, dto.getId());
		assertEquals("Harry Potter and the Philosopher's Stone", dto.getTitle());
		assertEquals("9780747532699", dto.getIsbn());
		assertEquals(1997, dto.getPublishedYear());
		assertEquals(1L, dto.getAuthorId());
	}

	@Test
	void findById_whenBookExists_returnsBookDto() {
		// Arrange
		Author author = createAuthor(1L, "Ali Safarli");
		Book book = createBook(1L, "Harry Potter and the Philosopher's Stone", "9780747532699", 1997, author);
		when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

		// Act
		BookDto result = bookService.findById(1L);

		// Assert
		assertEquals(1L, result.getId());
		assertEquals("Harry Potter and the Philosopher's Stone", result.getTitle());
		assertEquals(1L, result.getAuthorId());
	}

	@Test
	void findById_whenBookDoesNotExist_throwsResourceNotFoundException() {
		// Arrange
		when(bookRepository.findById(99L)).thenReturn(Optional.empty());

		// Act & Assert
		ResourceNotFoundException exception = assertThrows(
				ResourceNotFoundException.class,
				() -> bookService.findById(99L)
		);
		assertEquals("Book not found with id: 99", exception.getMessage());
	}

	@Test
	void create_whenAuthorExists_savesBookAndReturnsDto() {
		// Arrange
		Author author = createAuthor(1L, "Ali Safarli");
		BookDto request = createBookDto(null, "Harry Potter and the Chamber of Secrets", "9780747538493", 1998, 1L);

		when(authorRepository.findById(1L)).thenReturn(Optional.of(author));
		when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> {
			Book input = invocation.getArgument(0);
			Book saved = new Book();
			saved.setId(5L);
			saved.setTitle(input.getTitle());
			saved.setIsbn(input.getIsbn());
			saved.setPublishedYear(input.getPublishedYear());
			saved.setAuthor(input.getAuthor());
			return saved;
		});

		// Act
		BookDto result = bookService.create(request);

		// Assert
		assertEquals(5L, result.getId());
		assertEquals("Harry Potter and the Chamber of Secrets", result.getTitle());
		assertEquals("9780747538493", result.getIsbn());
		assertEquals(1998, result.getPublishedYear());
		assertEquals(1L, result.getAuthorId());

		ArgumentCaptor<Book> captor = ArgumentCaptor.forClass(Book.class);
		verify(bookRepository).save(captor.capture());
		assertNull(captor.getValue().getId());
		assertEquals(author, captor.getValue().getAuthor());
	}

	@Test
	void create_whenAuthorDoesNotExist_throwsResourceNotFoundException() {
		// Arrange
		BookDto request = createBookDto(null, "Harry Potter and the Chamber of Secrets", "9780747538493", 1998, 99L);
		when(authorRepository.findById(99L)).thenReturn(Optional.empty());

		// Act & Assert
		ResourceNotFoundException exception = assertThrows(
				ResourceNotFoundException.class,
				() -> bookService.create(request)
		);
		assertEquals("Author not found with id: 99", exception.getMessage());
		verify(bookRepository, never()).save(any(Book.class));
	}

	@Test
	void update_whenBookAndAuthorExist_updatesAndReturnsDto() {
		// Arrange
		Author oldAuthor = createAuthor(1L, "Ali Safarli");
		Author newAuthor = createAuthor(2L, "Omar Ismayilov");
		Book existing = createBook(1L, "Harry Potter and the Philosopher's Stone", "9780747532699", 1997, oldAuthor);
		BookDto request = createBookDto(null, "Harry Potter and the Chamber of Secrets", "9780747538493", 1998, 2L);

		when(bookRepository.findById(1L)).thenReturn(Optional.of(existing));
		when(authorRepository.findById(2L)).thenReturn(Optional.of(newAuthor));
		when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> invocation.getArgument(0));

		// Act
		BookDto result = bookService.update(1L, request);

		// Assert
		assertEquals(1L, result.getId());
		assertEquals("Harry Potter and the Chamber of Secrets", result.getTitle());
		assertEquals("9780747538493", result.getIsbn());
		assertEquals(1998, result.getPublishedYear());
		assertEquals(2L, result.getAuthorId());
		verify(bookRepository).save(existing);
	}

	@Test
	void update_whenBookDoesNotExist_throwsResourceNotFoundException() {
		// Arrange
		BookDto request = createBookDto(null, "Harry Potter and the Chamber of Secrets", "9780747538493", 1998, 1L);
		when(bookRepository.findById(99L)).thenReturn(Optional.empty());

		// Act & Assert
		assertThrows(ResourceNotFoundException.class, () -> bookService.update(99L, request));
		verify(bookRepository, never()).save(any(Book.class));
	}

	@Test
	void update_whenAuthorDoesNotExist_throwsResourceNotFoundException() {
		// Arrange
		Author author = createAuthor(1L, "Ali Safarli");
		Book existing = createBook(1L, "Harry Potter and the Philosopher's Stone", "9780747532699", 1997, author);
		BookDto request = createBookDto(null, "Harry Potter and the Chamber of Secrets", "9780747538493", 1998, 99L);

		when(bookRepository.findById(1L)).thenReturn(Optional.of(existing));
		when(authorRepository.findById(99L)).thenReturn(Optional.empty());

		// Act & Assert
		ResourceNotFoundException exception = assertThrows(
				ResourceNotFoundException.class,
				() -> bookService.update(1L, request)
		);
		assertEquals("Author not found with id: 99", exception.getMessage());
		verify(bookRepository, never()).save(any(Book.class));
	}

	@Test
	void delete_whenBookExists_deletesBook() {
		// Arrange
		when(bookRepository.existsById(1L)).thenReturn(true);

		// Act
		bookService.delete(1L);

		// Assert
		verify(bookRepository).deleteById(1L);
	}

	@Test
	void delete_whenBookDoesNotExist_throwsResourceNotFoundException() {
		// Arrange
		when(bookRepository.existsById(99L)).thenReturn(false);

		// Act & Assert
		ResourceNotFoundException exception = assertThrows(
				ResourceNotFoundException.class,
				() -> bookService.delete(99L)
		);
		assertEquals("Book not found with id: 99", exception.getMessage());
		verify(bookRepository, never()).deleteById(any());
	}

	private Author createAuthor(Long id, String name) {
		Author author = new Author();
		author.setId(id);
		author.setName(name);
		return author;
	}

	private Book createBook(Long id, String title, String isbn, Integer year, Author author) {
		Book book = new Book();
		book.setId(id);
		book.setTitle(title);
		book.setIsbn(isbn);
		book.setPublishedYear(year);
		book.setAuthor(author);
		return book;
	}

	private BookDto createBookDto(Long id, String title, String isbn, Integer year, Long authorId) {
		BookDto dto = new BookDto();
		dto.setId(id);
		dto.setTitle(title);
		dto.setIsbn(isbn);
		dto.setPublishedYear(year);
		dto.setAuthorId(authorId);
		return dto;
	}
}
