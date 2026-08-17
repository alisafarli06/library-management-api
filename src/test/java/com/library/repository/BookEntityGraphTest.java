package com.library.repository;

import com.library.entity.Author;
import com.library.entity.Book;
import com.library.entity.FileMetadata;
import com.library.service.BookService;
import jakarta.persistence.EntityManager;
import org.hibernate.Hibernate;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class BookEntityGraphTest {

	@Autowired
	private BookRepository bookRepository;

	@Autowired
	private AuthorRepository authorRepository;

	@Autowired
	private FileMetadataRepository fileMetadataRepository;

	@Autowired
	private BookService bookService;

	@Autowired
	private EntityManager entityManager;

	private Author author;
	private Book book;

	@BeforeEach
	void setUp() {
		author = new Author();
		author.setName("Graph Author " + UUID.randomUUID());
		author = authorRepository.save(author);

		book = new Book();
		book.setTitle("Graph Book");
		book.setIsbn(UUID.randomUUID().toString().replace("-", "").substring(0, 13));
		book.setPublishedYear(2024);
		book.setAuthor(author);
		book = bookRepository.save(book);
	}

	@Test
	void findAll_withEntityGraph_initializesAuthor() {
		Page<Book> page = bookRepository.findAll(PageRequest.of(0, 20));

		Book loaded = page.getContent().stream()
				.filter(item -> item.getId().equals(book.getId()))
				.findFirst()
				.orElseThrow();

		assertTrue(Hibernate.isInitialized(loaded.getAuthor()));
		assertEquals(author.getId(), loaded.getAuthor().getId());
	}

	@Test
	void findAll_withEntityGraph_initializesCoverAndPrefaceFiles() {
		FileMetadata cover = saveFile("cover.png", "image/png");
		FileMetadata preface = saveFile("preface.pdf", "application/pdf");
		book.setCoverFile(cover);
		book.setPrefaceFile(preface);
		book = bookRepository.save(book);
		entityManager.flush();
		entityManager.clear();

		Page<Book> page = bookRepository.findAll(PageRequest.of(0, 50));
		Book loaded = page.getContent().stream()
				.filter(item -> item.getId().equals(book.getId()))
				.findFirst()
				.orElseThrow();

		assertTrue(Hibernate.isInitialized(loaded.getCoverFile()));
		assertTrue(Hibernate.isInitialized(loaded.getPrefaceFile()));
		assertEquals(cover.getId(), loaded.getCoverFile().getId());
		assertEquals("cover.png", loaded.getCoverFile().getOriginalFilename());
		assertEquals(preface.getId(), loaded.getPrefaceFile().getId());
		assertEquals("preface.pdf", loaded.getPrefaceFile().getOriginalFilename());
	}

	@Test
	void findAll_serviceStillMapsAuthorId() {
		var result = bookService.findAll(PageRequest.of(0, 50));

		var dto = result.getContent().stream()
				.filter(item -> item.getId().equals(book.getId()))
				.findFirst()
				.orElseThrow();

		assertNotNull(dto.getAuthorId());
		assertEquals(author.getId(), dto.getAuthorId());
	}

	@Test
	void findAll_withEntityGraph_doesNotIssueNPlusOneAuthorQueries() {
		Author secondAuthor = saveAuthor("Graph Author B");
		Author thirdAuthor = saveAuthor("Graph Author C");
		saveBook("Graph Book B", secondAuthor);
		saveBook("Graph Book C", thirdAuthor);

		entityManager.flush();
		entityManager.clear();

		Statistics statistics = entityManager.getEntityManagerFactory()
				.unwrap(SessionFactory.class)
				.getStatistics();
		statistics.setStatisticsEnabled(true);
		statistics.clear();

		Page<Book> page = bookRepository.findAll(PageRequest.of(0, 50));
		List<Book> loaded = page.getContent();
		assertTrue(loaded.size() >= 3);

		for (Book item : loaded) {
			assertTrue(Hibernate.isInitialized(item.getAuthor()));
			assertNotNull(item.getAuthor().getName());
			if (item.getCoverFile() != null) {
				assertTrue(Hibernate.isInitialized(item.getCoverFile()));
				assertNotNull(item.getCoverFile().getOriginalFilename());
			}
			if (item.getPrefaceFile() != null) {
				assertTrue(Hibernate.isInitialized(item.getPrefaceFile()));
				assertNotNull(item.getPrefaceFile().getOriginalFilename());
			}
		}

		long statementCount = statistics.getPrepareStatementCount();
		assertTrue(
				statementCount <= 2,
				() -> "EntityGraph should load books, authors, and files without N+1 (expected at most count + select, got "
						+ statementCount + " statements)"
		);
	}

	@Test
	void findAllBySpecification_withEntityGraph_initializesAuthorAndFiles() {
		FileMetadata cover = saveFile("search-cover.png", "image/png");
		FileMetadata preface = saveFile("search-preface.pdf", "application/pdf");
		book.setCoverFile(cover);
		book.setPrefaceFile(preface);
		book = bookRepository.save(book);
		entityManager.flush();
		entityManager.clear();

		Page<Book> page = bookRepository.findAll(
				BookSpecifications.titleContains(book.getTitle()),
				PageRequest.of(0, 20)
		);
		Book loaded = page.getContent().stream()
				.filter(item -> item.getId().equals(book.getId()))
				.findFirst()
				.orElseThrow();

		assertTrue(Hibernate.isInitialized(loaded.getAuthor()));
		assertTrue(Hibernate.isInitialized(loaded.getCoverFile()));
		assertTrue(Hibernate.isInitialized(loaded.getPrefaceFile()));
		assertEquals("search-cover.png", loaded.getCoverFile().getOriginalFilename());
		assertEquals("search-preface.pdf", loaded.getPrefaceFile().getOriginalFilename());
	}

	private Author saveAuthor(String namePrefix) {
		Author created = new Author();
		created.setName(namePrefix + " " + UUID.randomUUID());
		return authorRepository.save(created);
	}

	private Book saveBook(String title, Author bookAuthor) {
		Book created = new Book();
		created.setTitle(title);
		created.setIsbn(UUID.randomUUID().toString().replace("-", "").substring(0, 13));
		created.setPublishedYear(2024);
		created.setAuthor(bookAuthor);
		return bookRepository.save(created);
	}

	private FileMetadata saveFile(String originalFilename, String contentType) {
		FileMetadata file = new FileMetadata();
		file.setOriginalFilename(originalFilename);
		file.setStoredFilename(UUID.randomUUID() + "-" + originalFilename);
		file.setContentType(contentType);
		file.setSize(32);
		file.setCreatedAt(Instant.now());
		return fileMetadataRepository.save(file);
	}
}
