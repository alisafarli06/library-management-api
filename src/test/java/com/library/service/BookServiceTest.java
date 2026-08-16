package com.library.service;

import com.library.dto.BookDto;
import com.library.dto.BookSearchRequest;
import com.library.dto.FileMetadataDto;
import com.library.entity.Author;
import com.library.entity.Book;
import com.library.entity.FileMetadata;
import com.library.exception.BadRequestException;
import com.library.exception.ResourceNotFoundException;
import com.library.mapper.BookMapper;
import com.library.repository.AuthorRepository;
import com.library.repository.BookRepository;
import com.library.repository.FileMetadataRepository;
import com.library.service.FileService;
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
import org.springframework.data.jpa.domain.Specification;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

	@Mock
	private BookRepository bookRepository;

	@Mock
	private AuthorRepository authorRepository;

	@Mock
	private FileMetadataRepository fileMetadataRepository;

	@Mock
	private FileService fileService;

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
		assertTrue(dto.isAvailable());
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
		assertTrue(result.isAvailable());
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
		assertTrue(captor.getValue().isAvailable());
		assertTrue(result.isAvailable());
	}

	@Test
	void create_ignoresAvailableFlagFromRequest() {
		Author author = createAuthor(1L, "Ali Safarli");
		BookDto request = createBookDto(null, "Harry Potter and the Chamber of Secrets", "9780747538493", 1998, 1L);
		request.setAvailable(false);

		when(authorRepository.findById(1L)).thenReturn(Optional.of(author));
		when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> {
			Book input = invocation.getArgument(0);
			input.setId(5L);
			return input;
		});

		BookDto result = bookService.create(request);

		assertTrue(result.isAvailable());
		ArgumentCaptor<Book> captor = ArgumentCaptor.forClass(Book.class);
		verify(bookRepository).save(captor.capture());
		assertTrue(captor.getValue().isAvailable());
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
		existing.setAvailable(false);
		BookDto request = createBookDto(null, "Harry Potter and the Chamber of Secrets", "9780747538493", 1998, 2L);
		request.setAvailable(true);

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
		assertFalse(result.isAvailable());
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
		Author author = createAuthor(1L, "Ali Safarli");
		Book existing = createBook(1L, "Harry Potter and the Philosopher's Stone", "9780747532699", 1997, author);
		when(bookRepository.findById(1L)).thenReturn(Optional.of(existing));
		when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> invocation.getArgument(0));

		bookService.delete(1L);

		verify(bookRepository).delete(existing);
	}

	@Test
	void delete_whenBookDoesNotExist_throwsResourceNotFoundException() {
		when(bookRepository.findById(99L)).thenReturn(Optional.empty());

		ResourceNotFoundException exception = assertThrows(
				ResourceNotFoundException.class,
				() -> bookService.delete(99L)
		);
		assertEquals("Book not found with id: 99", exception.getMessage());
		verify(bookRepository, never()).delete(any(Book.class));
	}

	@Test
	void create_withoutFiles_leavesAttachmentIdsNull() {
		Author author = createAuthor(1L, "Ali Safarli");
		BookDto request = createBookDto(null, "Harry Potter and the Chamber of Secrets", "9780747538493", 1998, 1L);
		when(authorRepository.findById(1L)).thenReturn(Optional.of(author));
		when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> {
			Book input = invocation.getArgument(0);
			input.setId(5L);
			return input;
		});

		BookDto result = bookService.create(request);

		assertNull(result.getCoverFileId());
		assertNull(result.getPrefaceFileId());
	}

	@Test
	void attachCover_whenJpeg_setsCoverMetadata() {
		Author author = createAuthor(1L, "Ali Safarli");
		Book existing = createBook(1L, "Harry Potter and the Philosopher's Stone", "9780747532699", 1997, author);
		when(bookRepository.findById(1L)).thenReturn(Optional.of(existing));
		when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> invocation.getArgument(0));
		FileMetadataDto uploaded = new FileMetadataDto();
		uploaded.setId(40L);
		when(fileService.upload(any())).thenReturn(uploaded);
		FileMetadata jpeg = new FileMetadata();
		jpeg.setId(40L);
		jpeg.setContentType("image/jpeg");
		jpeg.setOriginalFilename("cover.jpg");
		when(fileMetadataRepository.findById(40L)).thenReturn(Optional.of(jpeg));

		BookDto result = bookService.attachCover(1L, new MockMultipartFile("file", "cover.jpg", "image/jpeg", new byte[] {
				(byte) 0xFF, (byte) 0xD8, (byte) 0xFF
		}));

		assertEquals(40L, result.getCoverFileId());
		assertEquals("cover.jpg", result.getCoverFileName());
		assertNull(result.getPrefaceFileId());
	}

	@Test
	void attachCover_rejectsPdf() {
		Author author = createAuthor(1L, "Ali Safarli");
		Book existing = createBook(1L, "Harry Potter and the Philosopher's Stone", "9780747532699", 1997, author);
		when(bookRepository.findById(1L)).thenReturn(Optional.of(existing));
		FileMetadataDto uploaded = new FileMetadataDto();
		uploaded.setId(40L);
		when(fileService.upload(any())).thenReturn(uploaded);
		FileMetadata pdf = new FileMetadata();
		pdf.setId(40L);
		pdf.setContentType("application/pdf");
		pdf.setOriginalFilename("intro.pdf");
		when(fileMetadataRepository.findById(40L)).thenReturn(Optional.of(pdf));

		BadRequestException exception = assertThrows(
				BadRequestException.class,
				() -> bookService.attachCover(1L, new MockMultipartFile("file", "intro.pdf", "application/pdf", "%PDF-1".getBytes()))
		);
		assertEquals("Cover must be a JPEG or PNG image", exception.getMessage());
		verify(fileService).delete(40L);
	}

	@Test
	void attachPreface_rejectsJpeg() {
		Author author = createAuthor(1L, "Ali Safarli");
		Book existing = createBook(1L, "Harry Potter and the Philosopher's Stone", "9780747532699", 1997, author);
		when(bookRepository.findById(1L)).thenReturn(Optional.of(existing));
		FileMetadataDto uploaded = new FileMetadataDto();
		uploaded.setId(41L);
		when(fileService.upload(any())).thenReturn(uploaded);
		FileMetadata jpeg = new FileMetadata();
		jpeg.setId(41L);
		jpeg.setContentType("image/jpeg");
		jpeg.setOriginalFilename("cover.jpg");
		when(fileMetadataRepository.findById(41L)).thenReturn(Optional.of(jpeg));

		BadRequestException exception = assertThrows(
				BadRequestException.class,
				() -> bookService.attachPreface(1L, new MockMultipartFile("file", "cover.jpg", "image/jpeg", new byte[] {
						(byte) 0xFF, (byte) 0xD8, (byte) 0xFF
				}))
		);
		assertEquals("Preface must be a PDF document", exception.getMessage());
		verify(fileService).delete(41L);
	}

	@Test
	void search_withCombinedFilters_delegatesToRepositoryAndMapsResults() {
		Author author = createAuthor(1L, "Craig Walls");
		Book book = createBook(1L, "Spring in Action", "9781617297571", 2022, author);
		BookSearchRequest request = new BookSearchRequest();
		request.setTitle("spring");
		request.setAuthor("craig");
		Pageable pageable = PageRequest.of(0, 10);

		when(bookRepository.findAll(any(Specification.class), eq(pageable)))
				.thenReturn(new PageImpl<>(List.of(book)));

		Page<BookDto> result = bookService.search(request, pageable);

		assertEquals(1, result.getTotalElements());
		assertEquals("Spring in Action", result.getContent().getFirst().getTitle());
		assertEquals(1L, result.getContent().getFirst().getAuthorId());
		assertTrue(result.getContent().getFirst().isAvailable());
		verify(bookRepository).findAll(any(Specification.class), eq(pageable));
	}

	@Test
	void search_whenNoMatches_returnsEmptyPage() {
		BookSearchRequest request = new BookSearchRequest();
		request.setTitle("missing");
		Pageable pageable = PageRequest.of(0, 10);

		when(bookRepository.findAll(any(Specification.class), eq(pageable)))
				.thenReturn(Page.empty(pageable));

		Page<BookDto> result = bookService.search(request, pageable);

		assertEquals(0, result.getTotalElements());
		assertTrue(result.getContent().isEmpty());
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
