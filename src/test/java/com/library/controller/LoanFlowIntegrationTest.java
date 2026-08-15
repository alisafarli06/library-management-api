package com.library.controller;

import com.library.entity.Author;
import com.library.entity.Book;
import com.library.entity.Loan;
import com.library.entity.Member;
import com.library.entity.Role;
import com.library.entity.User;
import com.library.repository.AuthorRepository;
import com.library.repository.BookRepository;
import com.library.repository.LoanRepository;
import com.library.repository.MemberRepository;
import com.library.repository.UserRepository;
import com.library.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class LoanFlowIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JwtService jwtService;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private AuthorRepository authorRepository;

	@Autowired
	private BookRepository bookRepository;

	@Autowired
	private LoanRepository loanRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Test
	void userSelfBorrowCreatesLoanAndReturnRestoresAvailability() throws Exception {
		User user = saveUser("Borrower User", Role.USER);
		Member member = saveMemberForUser(user);
		Book book = saveBook(true);
		String token = jwtService.generateAccessToken(user.getEmail(), Role.USER);

		mockMvc.perform(post("/api/user/books/" + book.getId() + "/borrow")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
				.andExpect(status().isNoContent());

		Loan loan = loanRepository.findByMember_IdAndBook_IdAndReturnedAtIsNull(member.getId(), book.getId()).orElseThrow();
		assertNotNull(loan.getBorrowedAt());
		assertNull(loan.getReturnedAt());
		assertFalse(bookRepository.findById(book.getId()).orElseThrow().isAvailable());

		mockMvc.perform(post("/api/user/books/" + book.getId() + "/return")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
				.andExpect(status().isNoContent());

		Loan returned = loanRepository.findById(loan.getId()).orElseThrow();
		assertNotNull(returned.getReturnedAt());
		assertTrue(bookRepository.findById(book.getId()).orElseThrow().isAvailable());
		assertEquals(1, loanRepository.findByMember_Id(member.getId(), org.springframework.data.domain.Pageable.unpaged()).getTotalElements());

		mockMvc.perform(post("/api/user/books/" + book.getId() + "/borrow")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
				.andExpect(status().isNoContent());

		assertEquals(2, loanRepository.findByMember_Id(member.getId(), org.springframework.data.domain.Pageable.unpaged()).getTotalElements());
		assertNotNull(loanRepository.findById(loan.getId()).orElseThrow().getReturnedAt());
	}

	@Test
	void adminMemberBorrowAndReturnCreateLoanHistory() throws Exception {
		User admin = userRepository.findByEmail("admin@library.com").orElseGet(() -> saveUser("Library Admin", Role.ADMIN));
		Member member = saveMember("Admin Target");
		Book book = saveBook(true);
		String token = jwtService.generateAccessToken(admin.getEmail(), Role.ADMIN);

		mockMvc.perform(post("/api/members/" + member.getId() + "/books/" + book.getId() + "/borrow")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
				.andExpect(status().isNoContent());

		Loan loan = loanRepository.findByMember_IdAndBook_IdAndReturnedAtIsNull(member.getId(), book.getId()).orElseThrow();
		assertNotNull(loan.getBorrowedAt());
		assertNull(loan.getReturnedAt());

		mockMvc.perform(post("/api/members/" + member.getId() + "/books/" + book.getId() + "/return")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
				.andExpect(status().isNoContent());

		assertNotNull(loanRepository.findById(loan.getId()).orElseThrow().getReturnedAt());
		assertTrue(bookRepository.findById(book.getId()).orElseThrow().isAvailable());
	}

	@Test
	void missingMemberBookAndLoanProduceExpectedErrors() throws Exception {
		User admin = userRepository.findByEmail("admin@library.com").orElseGet(() -> saveUser("Library Admin", Role.ADMIN));
		String token = jwtService.generateAccessToken(admin.getEmail(), Role.ADMIN);
		Member member = saveMember("Error Target");
		Book book = saveBook(true);

		mockMvc.perform(post("/api/members/999999/books/" + book.getId() + "/borrow")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message").value("Member not found with id: 999999"));

		mockMvc.perform(post("/api/members/" + member.getId() + "/books/999999/borrow")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message").value("Book not found with id: 999999"));

		mockMvc.perform(post("/api/members/" + member.getId() + "/books/" + book.getId() + "/return")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message").value("Active loan not found"));
	}

	@Test
	void userLoanHistoryIsScopedAndAdminSeesAll() throws Exception {
		User firstUser = saveUser("First Historian", Role.USER);
		User secondUser = saveUser("Second Historian", Role.USER);
		Member firstMember = saveMemberForUser(firstUser);
		Member secondMember = saveMemberForUser(secondUser);
		Book firstBook = saveBook(true);
		Book secondBook = saveBook(true);
		String firstToken = jwtService.generateAccessToken(firstUser.getEmail(), Role.USER);
		String secondToken = jwtService.generateAccessToken(secondUser.getEmail(), Role.USER);
		String adminToken = jwtService.generateAccessToken(
				userRepository.findByEmail("admin@library.com").orElseGet(() -> saveUser("Library Admin", Role.ADMIN)).getEmail(),
				Role.ADMIN);

		mockMvc.perform(post("/api/user/books/" + firstBook.getId() + "/borrow")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + firstToken))
				.andExpect(status().isNoContent());
		mockMvc.perform(post("/api/user/books/" + secondBook.getId() + "/borrow")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + secondToken))
				.andExpect(status().isNoContent());

		mockMvc.perform(get("/api/user/loans")
						.param("page", "0")
						.param("size", "20")
						.param("sort", "borrowedAt,desc")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + firstToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content.length()").value(1))
				.andExpect(jsonPath("$.content[0].memberId").value(firstMember.getId().intValue()))
				.andExpect(jsonPath("$.content[0].bookId").value(firstBook.getId().intValue()))
				.andExpect(jsonPath("$.content[0].borrowedAt").exists())
				.andExpect(jsonPath("$.totalElements").value(1));

		mockMvc.perform(get("/api/loans")
						.param("page", "0")
						.param("size", "20")
						.param("sort", "borrowedAt,desc")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content").isArray())
				.andExpect(jsonPath("$.size").value(20))
				.andExpect(jsonPath("$.totalElements").value(org.hamcrest.Matchers.greaterThanOrEqualTo(2)));
	}

	@Test
	void activeDuplicateLoanIsRejectedByDatabaseConstraint() {
		Member member = saveMember("Constraint Member");
		Book book = saveBook(true);

		Loan first = new Loan();
		first.setMember(member);
		first.setBook(book);
		first.setBorrowedAt(Instant.now());
		loanRepository.saveAndFlush(first);

		Loan duplicate = new Loan();
		duplicate.setMember(member);
		duplicate.setBook(book);
		duplicate.setBorrowedAt(Instant.now());

		assertThrows(DataIntegrityViolationException.class, () -> loanRepository.saveAndFlush(duplicate));
	}

	private User saveUser(String fullName, Role role) {
		User user = new User();
		user.setFullName(fullName);
		user.setEmail(fullName.toLowerCase().replace(' ', '-') + "-" + UUID.randomUUID() + "@library.com");
		user.setPassword(passwordEncoder.encode("User12345"));
		user.setRole(role);
		return userRepository.save(user);
	}

	private Member saveMember(String name) {
		Member member = new Member();
		member.setName(name);
		member.setEmail(name.toLowerCase().replace(' ', '-') + "-" + UUID.randomUUID() + "@library.com");
		return memberRepository.save(member);
	}

	private Member saveMemberForUser(User user) {
		Member member = new Member();
		member.setName(user.getFullName());
		member.setEmail(user.getEmail());
		member.setUser(user);
		return memberRepository.save(member);
	}

	private Book saveBook(boolean available) {
		Author author = new Author();
		author.setName("Loan Author " + UUID.randomUUID());
		author = authorRepository.save(author);
		Book book = new Book();
		book.setTitle("Loan Book " + UUID.randomUUID());
		book.setIsbn(UUID.randomUUID().toString().replace("-", "").substring(0, 13));
		book.setPublishedYear(2024);
		book.setAuthor(author);
		book.setAvailable(available);
		return bookRepository.save(book);
	}
}
