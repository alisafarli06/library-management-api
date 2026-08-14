package com.library.service;

import com.library.entity.Author;
import com.library.entity.Book;
import com.library.entity.Member;
import com.library.exception.ConflictException;
import com.library.repository.AuthorRepository;
import com.library.repository.BookRepository;
import com.library.repository.MemberRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class ConcurrentBorrowBookTest {

	@Autowired
	private MemberService memberService;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private AuthorRepository authorRepository;

	@Autowired
	private BookRepository bookRepository;

	private Author author;
	private Book book;
	private Member firstMember;
	private Member secondMember;

	@BeforeEach
	void setUp() {
		author = new Author();
		author.setName("Concurrent Author " + UUID.randomUUID());
		author = authorRepository.save(author);

		book = new Book();
		book.setTitle("Concurrent Book");
		book.setIsbn(UUID.randomUUID().toString().replace("-", "").substring(0, 13));
		book.setPublishedYear(2024);
		book.setAuthor(author);
		book = bookRepository.save(book);

		firstMember = saveMember("First Concurrent");
		secondMember = saveMember("Second Concurrent");
	}

	@AfterEach
	void tearDown() {
		deleteIfPresent(firstMember, secondMember);
		if (book != null && book.getId() != null && bookRepository.existsById(book.getId())) {
			bookRepository.deleteById(book.getId());
		}
		if (author != null && author.getId() != null && authorRepository.existsById(author.getId())) {
			authorRepository.deleteById(author.getId());
		}
	}

	@Test
	void concurrentBorrow_sameBook_onlyOneSucceeds() throws Exception {
		CountDownLatch start = new CountDownLatch(1);
		AtomicInteger successes = new AtomicInteger();
		AtomicInteger conflicts = new AtomicInteger();
		List<Throwable> unexpected = new ArrayList<>();

		ExecutorService executor = Executors.newFixedThreadPool(2);
		try {
			Future<?> first = executor.submit(() -> borrowWhenReady(firstMember.getId(), start, successes, conflicts, unexpected));
			Future<?> second = executor.submit(() -> borrowWhenReady(secondMember.getId(), start, successes, conflicts, unexpected));

			start.countDown();
			first.get(15, TimeUnit.SECONDS);
			second.get(15, TimeUnit.SECONDS);
		} finally {
			executor.shutdownNow();
		}

		assertTrue(unexpected.isEmpty(), () -> "Unexpected errors: " + unexpected);
		assertEquals(1, successes.get(), "Exactly one concurrent borrow should succeed");
		assertEquals(1, conflicts.get(), "The other concurrent borrow should be rejected as conflict");

		long borrowCount = 0;
		if (memberRepository.existsByIdAndBooks_Id(firstMember.getId(), book.getId())) {
			borrowCount++;
		}
		if (memberRepository.existsByIdAndBooks_Id(secondMember.getId(), book.getId())) {
			borrowCount++;
		}
		assertEquals(1, borrowCount);
		assertFalse(bookRepository.findById(book.getId()).orElseThrow().isAvailable());
	}

	private void borrowWhenReady(
			Long memberId,
			CountDownLatch start,
			AtomicInteger successes,
			AtomicInteger conflicts,
			List<Throwable> unexpected) {
		try {
			if (!start.await(10, TimeUnit.SECONDS)) {
				unexpected.add(new IllegalStateException("Timed out waiting to start borrow for member " + memberId));
				return;
			}
			memberService.borrowBook(memberId, book.getId());
			successes.incrementAndGet();
		} catch (ConflictException ex) {
			conflicts.incrementAndGet();
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
			unexpected.add(ex);
		} catch (RuntimeException ex) {
			unexpected.add(ex);
		}
	}

	private Member saveMember(String name) {
		Member member = new Member();
		member.setName(name);
		member.setEmail(name.toLowerCase().replace(' ', '-') + "-" + UUID.randomUUID() + "@library.com");
		return memberRepository.save(member);
	}

	private void deleteIfPresent(Member... members) {
		for (Member member : members) {
			if (member != null && member.getId() != null && memberRepository.existsById(member.getId())) {
				memberRepository.deleteById(member.getId());
			}
		}
	}
}
