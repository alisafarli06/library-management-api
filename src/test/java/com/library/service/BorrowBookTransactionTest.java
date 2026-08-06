package com.library.service;

import com.library.entity.Author;
import com.library.entity.Book;
import com.library.entity.Member;
import com.library.repository.AuthorRepository;
import com.library.repository.BookRepository;
import com.library.repository.MemberRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;

@SpringBootTest
class BorrowBookTransactionTest {

	@Autowired
	private MemberService memberService;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private AuthorRepository authorRepository;

	@MockitoSpyBean
	private BookRepository bookRepository;

	private Author author;
	private Member member;
	private Book book;

	@BeforeEach
	void setUp() {
		reset(bookRepository);

		author = new Author();
		author.setName("Borrow Author " + UUID.randomUUID());
		author = authorRepository.save(author);

		book = new Book();
		book.setTitle("Borrowable Book");
		book.setIsbn(UUID.randomUUID().toString().replace("-", "").substring(0, 13));
		book.setPublishedYear(2024);
		book.setAuthor(author);
		book = bookRepository.save(book);

		member = new Member();
		member.setName("Borrower");
		member.setEmail("borrower-" + UUID.randomUUID() + "@library.com");
		member = memberRepository.save(member);
	}

	@AfterEach
	void tearDown() {
		reset(bookRepository);
		if (member != null && member.getId() != null) {
			memberRepository.deleteById(member.getId());
		}
		if (book != null && book.getId() != null) {
			bookRepository.deleteById(book.getId());
		}
		if (author != null && author.getId() != null) {
			authorRepository.deleteById(author.getId());
		}
	}

	@Test
	@Transactional
	void borrowBook_successfulTransaction_persistsRelationship() {
		memberService.borrowBook(member.getId(), book.getId());

		assertTrue(memberRepository.existsByIdAndBooks_Id(member.getId(), book.getId()));
		assertTrue(bookRepository.existsByIdAndMembers_Id(book.getId(), member.getId()));
	}

	@Test
	void borrowBook_whenBookSaveFails_rollsBackRelationship() {
		doThrow(new RuntimeException("forced failure")).when(bookRepository).save(any(Book.class));

		assertThrows(RuntimeException.class, () -> memberService.borrowBook(member.getId(), book.getId()));

		assertFalse(memberRepository.existsByIdAndBooks_Id(member.getId(), book.getId()));
		assertTrue(bookRepository.findByMembersIsEmpty().stream()
				.anyMatch(availableBook -> availableBook.getId().equals(book.getId())));
	}
}
