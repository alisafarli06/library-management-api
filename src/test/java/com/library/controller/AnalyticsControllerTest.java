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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AnalyticsControllerTest {

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

	private String adminToken;
	private String userToken;

	@BeforeEach
	void setUp() {
		loanRepository.deleteAll();
		User admin = userRepository.findByEmail("admin@library.com")
				.orElseGet(() -> saveUser("Library Admin", Role.ADMIN, "admin@library.com"));
		User user = userRepository.findByEmail("user@library.com")
				.orElseGet(() -> saveUser("Test User", Role.USER, "user@library.com"));
		adminToken = jwtService.generateAccessToken(admin.getEmail(), Role.ADMIN);
		userToken = jwtService.generateAccessToken(user.getEmail(), Role.USER);
	}

	@Test
	void userCannotAccessAnalyticsEndpoints() throws Exception {
		mockMvc.perform(get("/api/admin/analytics/summary")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.message").value("Access denied"));
		mockMvc.perform(get("/api/admin/analytics/books")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.message").value("Access denied"));
		mockMvc.perform(get("/api/admin/analytics/authors")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.message").value("Access denied"));
		mockMvc.perform(get("/api/admin/analytics/members")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.message").value("Access denied"));
	}

	@Test
	void unauthenticatedAnalyticsRequestsAreUnauthorized() throws Exception {
		mockMvc.perform(get("/api/admin/analytics/summary"))
				.andExpect(status().isUnauthorized());
		mockMvc.perform(get("/api/admin/analytics/books"))
				.andExpect(status().isUnauthorized());
		mockMvc.perform(get("/api/admin/analytics/authors"))
				.andExpect(status().isUnauthorized());
		mockMvc.perform(get("/api/admin/analytics/members"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void adminCanAccessAllAnalyticsEndpointsWhenEmpty() throws Exception {
		mockMvc.perform(get("/api/admin/analytics/summary")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.totalLoans").value(0))
				.andExpect(jsonPath("$.activeLoans").value(0))
				.andExpect(jsonPath("$.returnedLoans").value(0))
				.andExpect(jsonPath("$.totalBooksBorrowed").value(0))
				.andExpect(jsonPath("$.totalMembersWithLoans").value(0));

		mockMvc.perform(get("/api/admin/analytics/books")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content").isEmpty())
				.andExpect(jsonPath("$.totalElements").value(0))
				.andExpect(jsonPath("$.size").value(10));
		mockMvc.perform(get("/api/admin/analytics/authors")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content").isEmpty())
				.andExpect(jsonPath("$.totalElements").value(0));
		mockMvc.perform(get("/api/admin/analytics/members")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content").isEmpty())
				.andExpect(jsonPath("$.totalElements").value(0));
	}

	@Test
	void summaryCountsLoansBooksAndMembers() throws Exception {
		Member firstMember = saveMember("Ada Lovelace");
		Member secondMember = saveMember("Grace Hopper");
		Book firstBook = saveBook("Clean Code");
		Book secondBook = saveBook("Effective Java");

		saveLoan(firstMember, firstBook, false);
		saveLoan(firstMember, secondBook, true);
		saveLoan(secondMember, firstBook, true);

		mockMvc.perform(get("/api/admin/analytics/summary")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.totalLoans").value(3))
				.andExpect(jsonPath("$.activeLoans").value(1))
				.andExpect(jsonPath("$.returnedLoans").value(2))
				.andExpect(jsonPath("$.totalBooksBorrowed").value(2))
				.andExpect(jsonPath("$.totalMembersWithLoans").value(2));
	}

	@Test
	void bookAnalyticsCountsRepeatsOrdersAndPaginates() throws Exception {
		Member member = saveMember("Ada Lovelace");
		Book popular = saveBook("Clean Code");
		Book mid = saveBook("Refactoring");
		Book rare = saveBook("Effective Java");

		saveLoan(member, popular, true);
		saveLoan(member, popular, true);
		saveLoan(member, popular, false);
		saveLoan(member, mid, true);
		saveLoan(member, mid, true);
		saveLoan(member, rare, true);

		mockMvc.perform(get("/api/admin/analytics/books")
						.param("page", "0")
						.param("size", "10")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.totalElements").value(3))
				.andExpect(jsonPath("$.content[0].bookId").value(popular.getId().intValue()))
				.andExpect(jsonPath("$.content[0].bookTitle").value("Clean Code"))
				.andExpect(jsonPath("$.content[0].borrowCount").value(3))
				.andExpect(jsonPath("$.content[1].bookId").value(mid.getId().intValue()))
				.andExpect(jsonPath("$.content[1].borrowCount").value(2))
				.andExpect(jsonPath("$.content[2].bookId").value(rare.getId().intValue()))
				.andExpect(jsonPath("$.content[2].borrowCount").value(1));

		mockMvc.perform(get("/api/admin/analytics/books")
						.param("page", "1")
						.param("size", "1")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.size").value(1))
				.andExpect(jsonPath("$.number").value(1))
				.andExpect(jsonPath("$.totalElements").value(3))
				.andExpect(jsonPath("$.totalPages").value(3))
				.andExpect(jsonPath("$.content.length()").value(1))
				.andExpect(jsonPath("$.content[0].bookId").value(mid.getId().intValue()));
	}

	@Test
	void bookAnalyticsUsesBookIdWhenBorrowCountsAreEqual() throws Exception {
		Member member = saveMember("Ada Lovelace");
		Book first = saveBook("Alpha");
		Book second = saveBook("Beta");
		saveLoan(member, first, true);
		saveLoan(member, second, true);

		mockMvc.perform(get("/api/admin/analytics/books")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content[0].bookId").value(first.getId().intValue()))
				.andExpect(jsonPath("$.content[1].bookId").value(second.getId().intValue()))
				.andExpect(jsonPath("$.content[0].borrowCount").value(1))
				.andExpect(jsonPath("$.content[1].borrowCount").value(1));
	}

	@Test
	void authorAnalyticsAggregatesThroughBooksAndPaginates() throws Exception {
		Author popularAuthor = saveAuthor("Robert C. Martin");
		Author otherAuthor = saveAuthor("Joshua Bloch");
		Book cleanCode = saveBook("Clean Code", popularAuthor);
		Book cleanArch = saveBook("Clean Architecture", popularAuthor);
		Book effectiveJava = saveBook("Effective Java", otherAuthor);
		Member member = saveMember("Ada Lovelace");

		saveLoan(member, cleanCode, true);
		saveLoan(member, cleanCode, true);
		saveLoan(member, cleanArch, true);
		saveLoan(member, effectiveJava, true);

		mockMvc.perform(get("/api/admin/analytics/authors")
						.param("page", "0")
						.param("size", "10")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.totalElements").value(2))
				.andExpect(jsonPath("$.content[0].authorId").value(popularAuthor.getId().intValue()))
				.andExpect(jsonPath("$.content[0].authorName").value("Robert C. Martin"))
				.andExpect(jsonPath("$.content[0].borrowCount").value(3))
				.andExpect(jsonPath("$.content[1].authorId").value(otherAuthor.getId().intValue()))
				.andExpect(jsonPath("$.content[1].borrowCount").value(1));

		mockMvc.perform(get("/api/admin/analytics/authors")
						.param("page", "1")
						.param("size", "1")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content.length()").value(1))
				.andExpect(jsonPath("$.content[0].authorId").value(otherAuthor.getId().intValue()))
				.andExpect(jsonPath("$.totalPages").value(2));
	}

	@Test
	void memberAnalyticsCountsOrdersAndOmitsEmail() throws Exception {
		Member active = saveMember("Ada Lovelace");
		Member quieter = saveMember("Grace Hopper");
		Book book = saveBook("Clean Code");

		saveLoan(active, book, true);
		saveLoan(active, book, true);
		saveLoan(active, book, false);
		saveLoan(quieter, book, true);

		mockMvc.perform(get("/api/admin/analytics/members")
						.param("page", "0")
						.param("size", "10")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.totalElements").value(2))
				.andExpect(jsonPath("$.content[0].memberId").value(active.getId().intValue()))
				.andExpect(jsonPath("$.content[0].memberName").value("Ada Lovelace"))
				.andExpect(jsonPath("$.content[0].borrowCount").value(3))
				.andExpect(jsonPath("$.content[0].email").doesNotExist())
				.andExpect(jsonPath("$.content[1].memberId").value(quieter.getId().intValue()))
				.andExpect(jsonPath("$.content[1].borrowCount").value(1));

		mockMvc.perform(get("/api/admin/analytics/members")
						.param("page", "1")
						.param("size", "1")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content[0].memberId").value(quieter.getId().intValue()))
				.andExpect(jsonPath("$.totalPages").value(2));
	}

	private User saveUser(String fullName, Role role, String email) {
		User user = new User();
		user.setFullName(fullName);
		user.setEmail(email);
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

	private Author saveAuthor(String name) {
		Author author = new Author();
		author.setName(name);
		return authorRepository.save(author);
	}

	private Book saveBook(String title) {
		return saveBook(title, saveAuthor("Analytics Author " + UUID.randomUUID()));
	}

	private Book saveBook(String title, Author author) {
		Book book = new Book();
		book.setTitle(title);
		book.setIsbn(UUID.randomUUID().toString().replace("-", "").substring(0, 13));
		book.setPublishedYear(2024);
		book.setAuthor(author);
		book.setAvailable(true);
		return bookRepository.save(book);
	}

	private Loan saveLoan(Member member, Book book, boolean returned) {
		Loan loan = new Loan();
		loan.setMember(member);
		loan.setBook(book);
		loan.setBorrowedAt(Instant.parse("2026-01-01T10:00:00Z"));
		if (returned) {
			loan.setReturnedAt(Instant.parse("2026-01-15T10:00:00Z"));
		}
		return loanRepository.saveAndFlush(loan);
	}
}
