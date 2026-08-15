package com.library.controller;

import com.library.entity.Author;
import com.library.entity.Book;
import com.library.entity.Member;
import com.library.entity.Role;
import com.library.entity.User;
import com.library.repository.AuthorRepository;
import com.library.repository.BookRepository;
import com.library.repository.MemberRepository;
import com.library.repository.UserRepository;
import com.library.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class BookAvailabilityIntegrationTest {

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
	private PasswordEncoder passwordEncoder;

	@Test
	void createdBookIsAvailableAndUpdateCannotChangeAvailability() throws Exception {
		Author author = saveAuthor();
		User admin = userRepository.findByEmail("admin@library.com").orElseGet(() -> saveUser("Library Admin", Role.ADMIN));
		String token = jwtService.generateAccessToken(admin.getEmail(), Role.ADMIN);

		String isbn = UUID.randomUUID().toString().replace("-", "").substring(0, 13);
		String createBody = """
				{"title":"Availability Create","isbn":"%s","publishedYear":2024,"authorId":%d,"available":false}
				""".formatted(isbn, author.getId());

		mockMvc.perform(post("/api/books")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
						.contentType("application/json")
						.content(createBody))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.available").value(true));

		Book created = bookRepository.findAll().stream()
				.filter((candidate) -> isbn.equals(candidate.getIsbn()))
				.findFirst()
				.orElseThrow();
		long bookId = created.getId();

		String updateBody = """
				{"title":"Availability Updated","isbn":"%s","publishedYear":2024,"authorId":%d,"available":true}
				""".formatted(isbn, author.getId());
		bookRepository.findById(bookId).orElseThrow().setAvailable(false);
		bookRepository.flush();

		mockMvc.perform(put("/api/books/" + bookId)
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
						.contentType("application/json")
						.content(updateBody))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.title").value("Availability Updated"))
				.andExpect(jsonPath("$.available").value(false));
	}

	@Test
	void borrowAndReturnAreReflectedInBookResponsesAndSearch() throws Exception {
		User user = saveUser("Availability User", Role.USER);
		saveMemberForUser(user);
		Book book = saveBook();
		String userToken = jwtService.generateAccessToken(user.getEmail(), Role.USER);
		String adminToken = jwtService.generateAccessToken(
				userRepository.findByEmail("admin@library.com").orElseGet(() -> saveUser("Library Admin", Role.ADMIN)).getEmail(),
				Role.ADMIN);

		mockMvc.perform(get("/api/books/" + book.getId())
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.available").value(true));

		mockMvc.perform(post("/api/user/books/" + book.getId() + "/borrow")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken))
				.andExpect(status().isNoContent());

		mockMvc.perform(get("/api/books/" + book.getId())
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.available").value(false));

		mockMvc.perform(get("/api/books/search")
						.param("available", "false")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content[?(@.id == " + book.getId() + ")].available").value(org.hamcrest.Matchers.hasItem(false)));

		mockMvc.perform(get("/api/books/search")
						.param("available", "true")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content[?(@.id == " + book.getId() + ")]").isEmpty());

		mockMvc.perform(post("/api/user/books/" + book.getId() + "/return")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken))
				.andExpect(status().isNoContent());

		mockMvc.perform(get("/api/books/" + book.getId())
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.available").value(true));
	}

	private User saveUser(String fullName, Role role) {
		User user = new User();
		user.setFullName(fullName);
		user.setEmail(fullName.toLowerCase().replace(' ', '-') + "-" + UUID.randomUUID() + "@library.com");
		user.setPassword(passwordEncoder.encode("User12345"));
		user.setRole(role);
		return userRepository.save(user);
	}

	private void saveMemberForUser(User user) {
		Member member = new Member();
		member.setName(user.getFullName());
		member.setEmail(user.getEmail());
		member.setUser(user);
		memberRepository.save(member);
	}

	private Author saveAuthor() {
		Author author = new Author();
		author.setName("Availability Author " + UUID.randomUUID());
		return authorRepository.save(author);
	}

	private Book saveBook() {
		Book book = new Book();
		book.setTitle("Availability Book " + UUID.randomUUID());
		book.setIsbn(UUID.randomUUID().toString().replace("-", "").substring(0, 13));
		book.setPublishedYear(2024);
		book.setAuthor(saveAuthor());
		book.setAvailable(true);
		return bookRepository.save(book);
	}
}
