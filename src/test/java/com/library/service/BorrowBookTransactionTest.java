package com.library.service;

import com.library.entity.Author;
import com.library.entity.Book;
import com.library.entity.Member;
import com.library.exception.ConflictException;
import com.library.repository.AuthorRepository;
import com.library.repository.BookRepository;
import com.library.repository.LoanRepository;
import com.library.repository.MemberRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

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

	@Autowired
	private LoanRepository loanRepository;

	@MockitoSpyBean
	private BookRepository bookRepository;

	private Author author;
	private Member member;
	private Book book;

	@BeforeEach
	void setUp() {
		reset(bookRepository);

		author = new Author();
		author.setName("Demo Borrow Author");
		author = authorRepository.save(author);

		book = new Book();
		book.setTitle("The Pragmatic Programmer");
		book.setIsbn(UUID.randomUUID().toString().replace("-", "").substring(0, 13));
		book.setPublishedYear(2024);
		book.setAuthor(author);
		book = bookRepository.save(book);

		member = new Member();
		member.setName("Casey Rivera");
		member.setEmail("borrower-" + UUID.randomUUID() + "@library.com");
		member = memberRepository.save(member);
	}

	@AfterEach
	void tearDown() {
		reset(bookRepository);
		if (member != null && member.getId() != null) {
			loanRepository.findByMember_Id(member.getId(), Pageable.unpaged()).forEach(loanRepository::delete);
			loanRepository.flush();
			if (memberRepository.existsById(member.getId())) {
				memberRepository.deleteById(member.getId());
			}
		}
		if (book != null && book.getId() != null && bookRepository.existsById(book.getId())) {
			bookRepository.deleteById(book.getId());
		}
		if (author != null && author.getId() != null && authorRepository.existsById(author.getId())) {
			authorRepository.deleteById(author.getId());
		}
	}

	@Test
	void borrowBook_successfulTransaction_persistsRelationshipAndMarksBookUnavailable() {
		assertFalse(memberRepository.existsByIdAndBooks_Id(member.getId(), book.getId()));
		assertTrue(reloadBook().isAvailable());

		memberService.borrowBook(member.getId(), book.getId());

		assertTrue(memberRepository.existsByIdAndBooks_Id(member.getId(), book.getId()));
		assertTrue(bookRepository.existsByIdAndMembers_Id(book.getId(), member.getId()));
		assertFalse(reloadBook().isAvailable(), "books.available must be updated to false");
	}


	@Test
	void borrowBook_whenBookSaveFails_rollsBackAllChanges() {
		assertFalse(
				memberRepository.existsByIdAndBooks_Id(member.getId(), book.getId()),
				"Before: member must not have borrowed the book"
		);
		assertFalse(
				bookRepository.existsByIdAndMembers_Id(book.getId(), member.getId()),
				"Before: book must not be linked to the member"
		);

		doThrow(new RuntimeException("forced failure during book save"))
				.when(bookRepository)
				.save(any(Book.class));

		RuntimeException thrown = assertThrows(
				RuntimeException.class,
				() -> memberService.borrowBook(member.getId(), book.getId())
		);
		assertTrue(thrown.getMessage().contains("forced failure"));

		assertFalse(
				memberRepository.existsByIdAndBooks_Id(member.getId(), book.getId()),
				"After rollback: no member_books join row should exist"
		);
		assertFalse(
				bookRepository.existsByIdAndMembers_Id(book.getId(), member.getId()),
				"After rollback: book must not reference the member"
		);
		assertTrue(
				bookRepository.findByMembersIsEmpty().stream()
						.anyMatch(availableBook -> availableBook.getId().equals(book.getId())),
				"After rollback: book must still be available"
		);
		assertTrue(reloadBook().isAvailable(), "After rollback: books.available must remain true");
	}

	@Test
	void borrowBook_whenBookIsUnavailable_isRejected() {
		book.setAvailable(false);
		book = bookRepository.save(book);

		assertThrows(ConflictException.class, () -> memberService.borrowBook(member.getId(), book.getId()));

		assertFalse(memberRepository.existsByIdAndBooks_Id(member.getId(), book.getId()));
		assertFalse(reloadBook().isAvailable());
		assertTrue(bookRepository.findByMembersIsEmpty().stream()
				.anyMatch(availableBook -> availableBook.getId().equals(book.getId())));
	}

	private Book reloadBook() {
		return bookRepository.findById(book.getId()).orElseThrow();
	}
}
