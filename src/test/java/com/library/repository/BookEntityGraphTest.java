package com.library.repository;

import com.library.entity.Author;
import com.library.entity.Book;
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
		}

		long statementCount = statistics.getPrepareStatementCount();
		assertTrue(
				statementCount <= 2,
				() -> "EntityGraph should load books+authors without N+1 (expected at most count + select, got "
						+ statementCount + " statements)"
		);
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
}
