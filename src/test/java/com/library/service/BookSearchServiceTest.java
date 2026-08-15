package com.library.service;

import com.library.dto.BookDto;
import com.library.dto.BookSearchRequest;
import com.library.entity.Author;
import com.library.entity.Book;
import com.library.entity.Member;
import com.library.repository.AuthorRepository;
import com.library.repository.BookRepository;
import com.library.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class BookSearchServiceTest {

	@Autowired
	private BookService bookService;

	@Autowired
	private BookRepository bookRepository;

	@Autowired
	private AuthorRepository authorRepository;

	@Autowired
	private MemberRepository memberRepository;

	private Book effectiveJava;
	private Book cleanCode;
	private Book springInAction;

	@BeforeEach
	void setUp() {
		Author martin = authorRepository.save(newAuthor("Robert Martin"));
		Author craig = authorRepository.save(newAuthor("Craig Walls"));

		effectiveJava = bookRepository.save(newBook("Effective Java", uniqueIsbn(), 2018, martin));
		cleanCode = bookRepository.save(newBook("Clean Code", uniqueIsbn(), 2008, martin));
		springInAction = bookRepository.save(newBook("Spring in Action", uniqueIsbn(), 2022, craig));
	}

	@Test
	void search_byTitleOnly_returnsMatchingBooks() {
		BookSearchRequest request = new BookSearchRequest();
		request.setTitle("java");

		Page<BookDto> result = bookService.search(request, PageRequest.of(0, 10));

		assertEquals(1, result.getTotalElements());
		assertEquals(effectiveJava.getId(), result.getContent().getFirst().getId());
	}

	@Test
	void search_byMultipleFilters_returnsOnlyMatchingBooks() {
		Member member = memberRepository.save(newMember("Dana", uniqueEmail()));
		member.borrowBook(springInAction);
		springInAction.setAvailable(false);
		memberRepository.save(member);
		bookRepository.save(springInAction);

		BookSearchRequest request = new BookSearchRequest();
		request.setTitle("java");
		request.setYearFrom(2015);
		request.setAvailable(true);

		Page<BookDto> result = bookService.search(request, PageRequest.of(0, 10));

		assertEquals(1, result.getTotalElements());
		assertEquals(effectiveJava.getId(), result.getContent().getFirst().getId());
	}

	@Test
	void search_withNoFilters_returnsPaginatedBooks() {
		BookSearchRequest request = new BookSearchRequest();

		Page<BookDto> result = bookService.search(request, PageRequest.of(0, 2));

		assertTrue(result.getTotalElements() >= 3);
		assertEquals(2, result.getContent().size());
		assertEquals(0, result.getNumber());
		assertEquals(2, result.getSize());
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
		return "search-" + UUID.randomUUID() + "@library.com";
	}
}
