package com.library.repository;

import com.library.entity.Author;
import com.library.entity.Book;
import com.library.entity.Member;
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
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class BookRepositoryTest {

	@Autowired
	private BookRepository bookRepository;

	@Autowired
	private AuthorRepository authorRepository;

	@Autowired
	private MemberRepository memberRepository;

	private Author martin;
	private Author craig;
	private Book effectiveJava;
	private Book cleanCode;
	private Book springInAction;

	@BeforeEach
	void setUp() {
		martin = authorRepository.save(newAuthor("Robert Martin"));
		craig = authorRepository.save(newAuthor("Craig Walls"));

		effectiveJava = bookRepository.save(newBook("Effective Java", uniqueIsbn(), 2018, martin));
		cleanCode = bookRepository.save(newBook("Clean Code", uniqueIsbn(), 2008, martin));
		springInAction = bookRepository.save(newBook("Spring in Action", uniqueIsbn(), 2022, craig));
	}

	@Test
	void findByTitleContainingIgnoreCase_returnsMatchesIgnoringCase() {
		List<Book> results = bookRepository.findByTitleContainingIgnoreCase("JAVA");

		assertEquals(1, results.size());
		assertEquals(effectiveJava.getId(), results.getFirst().getId());
	}

	@Test
	void findByTitleContainingIgnoreCase_returnsEmptyWhenNoMatch() {
		List<Book> results = bookRepository.findByTitleContainingIgnoreCase("nonexistent");

		assertTrue(results.isEmpty());
	}

	@Test
	void findByAuthorNameContainingIgnoreCase_returnsMultipleBooks() {
		List<Book> results = bookRepository.findByAuthor_NameContainingIgnoreCase("martin");

		assertEquals(2, results.size());
		assertTrue(results.stream().map(Book::getId).toList()
				.containsAll(List.of(effectiveJava.getId(), cleanCode.getId())));
	}

	@Test
	void findByPublishedYearGreaterThan_returnsBooksAfterYear() {
		List<Book> results = bookRepository.findByPublishedYearGreaterThan(2015);

		assertEquals(2, results.size());
		assertTrue(results.stream().map(Book::getId).toList()
				.containsAll(List.of(effectiveJava.getId(), springInAction.getId())));
	}

	@Test
	void findByPublishedYearBetween_returnsBooksInRange() {
		List<Book> results = bookRepository.findByPublishedYearBetween(2015, 2024);

		assertEquals(2, results.size());
		assertTrue(results.stream().map(Book::getId).toList()
				.containsAll(List.of(effectiveJava.getId(), springInAction.getId())));
	}

	@Test
	void findByMembersIsEmpty_returnsAvailableBooks() {
		Member member = memberRepository.save(newMember("Alice", uniqueEmail()));
		member.borrowBook(effectiveJava);
		memberRepository.save(member);

		List<Book> available = bookRepository.findByMembersIsEmpty();

		assertTrue(available.stream().map(Book::getId).toList()
				.containsAll(List.of(cleanCode.getId(), springInAction.getId())));
		assertTrue(available.stream().noneMatch(book -> book.getId().equals(effectiveJava.getId())));
	}

	@Test
	void findAvailablePublishedAfter_jpql_returnsOnlyAvailableBooksAfterYear() {
		Member member = memberRepository.save(newMember("Bob", uniqueEmail()));
		member.borrowBook(springInAction);
		memberRepository.save(member);

		List<Book> results = bookRepository.findAvailablePublishedAfter(2015);

		assertEquals(1, results.size());
		assertEquals(effectiveJava.getId(), results.getFirst().getId());
	}

	@Test
	void search_combinesTitleAndAuthorFilters() {
		Page<Book> results = bookRepository.findAll(
				BookSpecifications.titleContains("spring")
						.and(BookSpecifications.authorNameContains("craig")),
				PageRequest.of(0, 10)
		);

		assertEquals(1, results.getTotalElements());
		assertEquals(springInAction.getId(), results.getContent().getFirst().getId());
	}

	@Test
	void search_returnsEmptyForConflictingFilters() {
		Page<Book> results = bookRepository.findAll(
				BookSpecifications.titleContains("java")
						.and(BookSpecifications.authorNameContains("craig")),
				PageRequest.of(0, 10)
		);

		assertTrue(results.isEmpty());
	}

	@Test
	void search_withYearRangeAndAvailability() {
		Member member = memberRepository.save(newMember("Carol", uniqueEmail()));
		member.borrowBook(effectiveJava);
		memberRepository.save(member);

		Page<Book> results = bookRepository.findAll(
				BookSpecifications.publishedYearFrom(2015)
						.and(BookSpecifications.publishedYearTo(2024))
						.and(BookSpecifications.availability(true)),
				PageRequest.of(0, 10)
		);

		assertEquals(1, results.getTotalElements());
		assertEquals(springInAction.getId(), results.getContent().getFirst().getId());
	}

	private Author newAuthor(String name) {
		Author author = new Author();
		author.setName(name);
		return author;
	}

	private Book newBook(String title, String isbn, Integer year, Author author) {
		Book book = new Book();
		book.setTitle(title);
		book.setIsbn(isbn);
		book.setPublishedYear(year);
		book.setAuthor(author);
		return book;
	}

	private Member newMember(String name, String email) {
		Member member = new Member();
		member.setName(name);
		member.setEmail(email);
		return member;
	}

	private String uniqueIsbn() {
		return UUID.randomUUID().toString().replace("-", "").substring(0, 13);
	}

	private String uniqueEmail() {
		return "member-" + UUID.randomUUID() + "@library.com";
	}
}
