package com.library.entity;

import com.library.repository.AuthorRepository;
import com.library.repository.BookRepository;
import com.library.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class EntityRelationshipTest {

	@Autowired
	private AuthorRepository authorRepository;

	@Autowired
	private BookRepository bookRepository;

	@Autowired
	private MemberRepository memberRepository;

	@Test
	void authorOwnsMultipleBooks() {
		Author author = new Author();
		author.setName("Relationship Author");

		Book firstBook = newBook("First Book", uniqueIsbn());
		Book secondBook = newBook("Second Book", uniqueIsbn());
		author.addBook(firstBook);
		author.addBook(secondBook);

		Author savedAuthor = authorRepository.save(author);

		Author loadedAuthor = authorRepository.findById(savedAuthor.getId()).orElseThrow();
		assertEquals(2, loadedAuthor.getBooks().size());
		assertTrue(loadedAuthor.getBooks().stream()
				.allMatch(book -> book.getAuthor().getId().equals(savedAuthor.getId())));
	}

	@Test
	void memberCanBorrowMultipleBooks() {
		Author author = authorRepository.save(newAuthor("Borrow Author"));
		Book firstBook = bookRepository.save(newBook("Borrowed One", uniqueIsbn(), author));
		Book secondBook = bookRepository.save(newBook("Borrowed Two", uniqueIsbn(), author));

		Member member = new Member();
		member.setName("Borrowing Member");
		member.setEmail(uniqueEmail("borrower"));
		member.borrowBook(firstBook);
		member.borrowBook(secondBook);

		Member savedMember = memberRepository.save(member);

		Member loadedMember = memberRepository.findById(savedMember.getId()).orElseThrow();
		assertEquals(2, loadedMember.getBooks().size());
		assertTrue(loadedMember.getBooks().stream()
				.map(Book::getId)
				.toList()
				.containsAll(java.util.List.of(firstBook.getId(), secondBook.getId())));
	}

	@Test
	void bookCanBelongToMultipleMembers() {
		Author author = authorRepository.save(newAuthor("Shared Book Author"));
		Book book = bookRepository.save(newBook("Shared Book", uniqueIsbn(), author));

		Member firstMember = new Member();
		firstMember.setName("First Member");
		firstMember.setEmail(uniqueEmail("first"));
		firstMember.borrowBook(book);

		Member secondMember = new Member();
		secondMember.setName("Second Member");
		secondMember.setEmail(uniqueEmail("second"));
		secondMember.borrowBook(book);

		memberRepository.save(firstMember);
		memberRepository.save(secondMember);

		Book loadedBook = bookRepository.findById(book.getId()).orElseThrow();
		assertEquals(2, loadedBook.getMembers().size());
		assertTrue(loadedBook.getMembers().stream()
				.map(Member::getEmail)
				.toList()
				.containsAll(java.util.List.of(firstMember.getEmail(), secondMember.getEmail())));
	}

	private Author newAuthor(String name) {
		Author author = new Author();
		author.setName(name);
		return author;
	}

	private Book newBook(String title, String isbn) {
		Book book = new Book();
		book.setTitle(title);
		book.setIsbn(isbn);
		book.setPublishedYear(2024);
		return book;
	}

	private Book newBook(String title, String isbn, Author author) {
		Book book = newBook(title, isbn);
		book.setAuthor(author);
		return book;
	}

	private String uniqueIsbn() {
		return UUID.randomUUID().toString().replace("-", "").substring(0, 13);
	}

	private String uniqueEmail(String prefix) {
		return prefix + "-" + UUID.randomUUID() + "@library.com";
	}
}
