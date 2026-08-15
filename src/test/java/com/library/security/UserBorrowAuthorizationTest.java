package com.library.security;

import com.library.entity.Author;
import com.library.entity.Book;
import com.library.entity.Member;
import com.library.entity.Role;
import com.library.entity.User;
import com.library.repository.AuthorRepository;
import com.library.repository.BookRepository;
import com.library.repository.LoanRepository;
import com.library.repository.MemberRepository;
import com.library.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UserBorrowAuthorizationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JwtService jwtService;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private LoanRepository loanRepository;

	@Autowired
	private AuthorRepository authorRepository;

	@Autowired
	private BookRepository bookRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	private User borrower;
	private Member borrowerMember;
	private Member otherMember;
	private Author author;
	private Book availableBook;
	private Book unavailableBook;

	@BeforeEach
	void setUp() {
		borrower = new User();
		borrower.setFullName("Borrower User");
		borrower.setEmail("borrower-" + UUID.randomUUID() + "@library.com");
		borrower.setPassword(passwordEncoder.encode("User12345"));
		borrower.setRole(Role.USER);
		borrower = userRepository.save(borrower);

		borrowerMember = new Member();
		borrowerMember.setName(borrower.getFullName());
		borrowerMember.setEmail(borrower.getEmail());
		borrowerMember.setUser(borrower);
		borrowerMember = memberRepository.save(borrowerMember);

		otherMember = new Member();
		otherMember.setName("Other Member");
		otherMember.setEmail("other-" + UUID.randomUUID() + "@library.com");
		otherMember = memberRepository.save(otherMember);

		author = new Author();
		author.setName("Borrow Author " + UUID.randomUUID());
		author = authorRepository.save(author);

		availableBook = saveBook("Available Borrow Book", true);
		unavailableBook = saveBook("Unavailable Borrow Book", false);
	}

	@AfterEach
	void tearDown() {
		if (borrowerMember != null && borrowerMember.getId() != null) {
			loanRepository.findByMember_Id(borrowerMember.getId(), org.springframework.data.domain.Pageable.unpaged())
					.forEach(loanRepository::delete);
			memberRepository.deleteById(borrowerMember.getId());
		}
		if (otherMember != null && otherMember.getId() != null) {
			loanRepository.findByMember_Id(otherMember.getId(), org.springframework.data.domain.Pageable.unpaged())
					.forEach(loanRepository::delete);
			memberRepository.deleteById(otherMember.getId());
		}
		if (availableBook != null && availableBook.getId() != null) {
			bookRepository.deleteById(availableBook.getId());
		}
		if (unavailableBook != null && unavailableBook.getId() != null) {
			bookRepository.deleteById(unavailableBook.getId());
		}
		if (author != null && author.getId() != null) {
			authorRepository.deleteById(author.getId());
		}
		if (borrower != null && borrower.getId() != null) {
			userRepository.deleteById(borrower.getId());
		}
	}

	@Test
	void userCanBorrowOwnBookThroughUserEndpoint() throws Exception {
		String token = jwtService.generateAccessToken(borrower.getEmail(), Role.USER);

		mockMvc.perform(post("/api/user/books/" + availableBook.getId() + "/borrow")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
				.andExpect(status().isNoContent());
	}

	@Test
	void userCannotBorrowForAnotherMemberThroughMemberEndpoint() throws Exception {
		String token = jwtService.generateAccessToken(borrower.getEmail(), Role.USER);

		mockMvc.perform(post("/api/members/" + otherMember.getId() + "/books/" + availableBook.getId() + "/borrow")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.message").value("Access denied"));
	}

	@Test
	void adminCanBorrowOnBehalfOfMember() throws Exception {
		User admin = userRepository.findByEmail("admin@library.com").orElseGet(() -> {
			User created = new User();
			created.setFullName("Library Admin");
			created.setEmail("admin@library.com");
			created.setPassword(passwordEncoder.encode("Admin123!"));
			created.setRole(Role.ADMIN);
			return userRepository.save(created);
		});
		String token = jwtService.generateAccessToken(admin.getEmail(), Role.ADMIN);

		mockMvc.perform(post("/api/members/" + otherMember.getId() + "/books/" + availableBook.getId() + "/borrow")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
				.andExpect(status().isNoContent());
	}

	@Test
	void userBorrowWhenBookUnavailableReturnsConflict() throws Exception {
		String token = jwtService.generateAccessToken(borrower.getEmail(), Role.USER);

		mockMvc.perform(post("/api/user/books/" + unavailableBook.getId() + "/borrow")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.message").value("Book is not available"));
	}

	@Test
	void userDuplicateBorrowReturnsConflict() throws Exception {
		String token = jwtService.generateAccessToken(borrower.getEmail(), Role.USER);

		mockMvc.perform(post("/api/user/books/" + availableBook.getId() + "/borrow")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
				.andExpect(status().isNoContent());

		mockMvc.perform(post("/api/user/books/" + availableBook.getId() + "/borrow")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.message").value("Member already borrowed this book"));
	}

	@Test
	void userWithoutLinkedMemberReturnsNotFound() throws Exception {
		User orphan = new User();
		orphan.setFullName("Orphan User");
		orphan.setEmail("orphan-" + UUID.randomUUID() + "@library.com");
		orphan.setPassword(passwordEncoder.encode("User12345"));
		orphan.setRole(Role.USER);
		orphan = userRepository.save(orphan);
		String token = jwtService.generateAccessToken(orphan.getEmail(), Role.USER);

		try {
			mockMvc.perform(post("/api/user/books/" + availableBook.getId() + "/borrow")
							.header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
					.andExpect(status().isNotFound())
					.andExpect(jsonPath("$.message").value("Member not found for authenticated user"));
		} finally {
			userRepository.deleteById(orphan.getId());
		}
	}

	private Book saveBook(String title, boolean available) {
		Book book = new Book();
		book.setTitle(title);
		book.setIsbn(UUID.randomUUID().toString().replace("-", "").substring(0, 13));
		book.setPublishedYear(2024);
		book.setAuthor(author);
		book.setAvailable(available);
		return bookRepository.save(book);
	}
}
