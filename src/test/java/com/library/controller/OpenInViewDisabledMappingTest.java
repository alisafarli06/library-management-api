package com.library.controller;

import com.library.entity.AccountStatus;
import com.library.entity.Author;
import com.library.entity.Book;
import com.library.entity.FileMetadata;
import com.library.entity.Member;
import com.library.entity.Role;
import com.library.entity.User;
import com.library.repository.AuthorRepository;
import com.library.repository.BookRepository;
import com.library.repository.FileMetadataRepository;
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

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class OpenInViewDisabledMappingTest {

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
	private FileMetadataRepository fileMetadataRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	private String userToken;
	private String adminToken;
	private User linkedUser;
	private Member member;
	private Book book;
	private String uniqueTitle;

	@BeforeEach
	void setUp() {
		User reader = ensureUser("osiv-user@library.com", "OSIV User", Role.USER);
		User admin = ensureUser("osiv-admin@library.com", "OSIV Admin", Role.ADMIN);
		userToken = jwtService.generateToken(reader.getEmail(), Role.USER);
		adminToken = jwtService.generateToken(admin.getEmail(), Role.ADMIN);

		linkedUser = ensureUser("osiv-member-user@library.com", "OSIV Linked Member", Role.USER);
		member = memberRepository.findByEmail(linkedUser.getEmail()).orElseGet(Member::new);
		member.setName(linkedUser.getFullName());
		member.setEmail(linkedUser.getEmail());
		member.setUser(linkedUser);
		member = memberRepository.save(member);

		Author author = new Author();
		author.setName("OSIV Author " + UUID.randomUUID());
		author = authorRepository.save(author);

		uniqueTitle = "OSIV Cover Book " + UUID.randomUUID();
		book = new Book();
		book.setTitle(uniqueTitle);
		book.setIsbn(UUID.randomUUID().toString().replace("-", "").substring(0, 13));
		book.setPublishedYear(2021);
		book.setAuthor(author);
		book.setCoverFile(saveFile("osiv-cover.png", "image/png"));
		book.setPrefaceFile(saveFile("osiv-preface.pdf", "application/pdf"));
		book = bookRepository.save(book);
	}

	@Test
	void bookListMapsCoverAndPrefaceWhenOpenInViewIsDisabled() throws Exception {
		mockMvc.perform(get("/api/books")
						.param("size", "200")
						.param("sort", "id,desc")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content[?(@.id == " + book.getId() + ")].coverFileId",
						hasItem(book.getCoverFile().getId().intValue())))
				.andExpect(jsonPath("$.content[?(@.id == " + book.getId() + ")].coverFileName", hasItem("osiv-cover.png")))
				.andExpect(jsonPath("$.content[?(@.id == " + book.getId() + ")].prefaceFileId",
						hasItem(book.getPrefaceFile().getId().intValue())))
				.andExpect(jsonPath("$.content[?(@.id == " + book.getId() + ")].prefaceFileName", hasItem("osiv-preface.pdf")))
				.andExpect(jsonPath("$.content[?(@.id == " + book.getId() + ")].authorName",
						hasItem(book.getAuthor().getName())));
	}

	@Test
	void bookSearchMapsAttachmentsWhenOpenInViewIsDisabled() throws Exception {
		mockMvc.perform(get("/api/books/search")
						.param("title", uniqueTitle)
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.totalElements").value(1))
				.andExpect(jsonPath("$.content[0].id").value(book.getId()))
				.andExpect(jsonPath("$.content[0].coverFileId").value(book.getCoverFile().getId().intValue()))
				.andExpect(jsonPath("$.content[0].coverFileName").value("osiv-cover.png"))
				.andExpect(jsonPath("$.content[0].prefaceFileId").value(book.getPrefaceFile().getId().intValue()))
				.andExpect(jsonPath("$.content[0].prefaceFileName").value("osiv-preface.pdf"));
	}

	@Test
	void memberGetByIdMapsLinkedUserWhenOpenInViewIsDisabled() throws Exception {
		mockMvc.perform(get("/api/members/{id}", member.getId())
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(member.getId()))
				.andExpect(jsonPath("$.email").value(linkedUser.getEmail()))
				.andExpect(jsonPath("$.userId").value(linkedUser.getId()))
				.andExpect(jsonPath("$.role").value("USER"))
				.andExpect(jsonPath("$.status").value("ACTIVE"));
	}

	private User ensureUser(String email, String fullName, Role role) {
		User user = userRepository.findByEmail(email).orElseGet(User::new);
		user.setFullName(fullName);
		user.setEmail(email);
		user.setPassword(passwordEncoder.encode("User12345"));
		user.setRole(role);
		user.setStatus(AccountStatus.ACTIVE);
		return userRepository.save(user);
	}

	private FileMetadata saveFile(String originalFilename, String contentType) {
		FileMetadata file = new FileMetadata();
		file.setOriginalFilename(originalFilename);
		file.setStoredFilename(UUID.randomUUID() + "-" + originalFilename);
		file.setContentType(contentType);
		file.setSize(48);
		file.setCreatedAt(Instant.now());
		return fileMetadataRepository.save(file);
	}
}
