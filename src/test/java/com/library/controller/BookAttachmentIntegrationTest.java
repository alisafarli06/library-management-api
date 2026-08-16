package com.library.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.entity.Author;
import com.library.entity.Book;
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
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Path;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class BookAttachmentIntegrationTest {

	private static final byte[] PNG = new byte[] {
			(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0x00, 0x01
	};
	private static final byte[] JPEG = new byte[] {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x00};
	private static final byte[] PDF = "%PDF-1.4 preface".getBytes();

	@TempDir
	static Path tempDir;

	@DynamicPropertySource
	static void registerStorageDirectory(DynamicPropertyRegistry registry) {
		registry.add("app.file.storage-directory", () -> tempDir.toString());
	}

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JwtService jwtService;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private AuthorRepository authorRepository;

	@Autowired
	private BookRepository bookRepository;

	@Autowired
	private FileMetadataRepository fileMetadataRepository;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	private final ObjectMapper objectMapper = new ObjectMapper();

	private String userToken;
	private String adminToken;
	private Book book;

	@BeforeEach
	void setUp() {
		fileMetadataRepository.deleteAll();
		ensureUser("attach-user@library.com", Role.USER);
		ensureUser("attach-admin@library.com", Role.ADMIN);
		userToken = jwtService.generateToken("attach-user@library.com", Role.USER);
		adminToken = jwtService.generateToken("attach-admin@library.com", Role.ADMIN);

		Author author = new Author();
		author.setName("Attach Author " + UUID.randomUUID());
		author = authorRepository.save(author);
		book = new Book();
		book.setTitle("Attach Book");
		book.setIsbn(UUID.randomUUID().toString().replace("-", "").substring(0, 13));
		book.setPublishedYear(2020);
		book.setAuthor(author);
		book = bookRepository.save(book);
	}

	@Test
	void bookWithoutFilesExposesNullAttachmentFields() throws Exception {
		mockMvc.perform(get("/api/books/{id}", book.getId())
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.coverFileId").value(nullValue()))
				.andExpect(jsonPath("$.prefaceFileId").value(nullValue()))
				.andExpect(jsonPath("$.available").value(true));
	}

	@Test
	void adminCanAttachCoverAndUserCanDownloadIt() throws Exception {
		MvcResult coverResult = mockMvc.perform(multipart("/api/books/{id}/cover", book.getId())
						.file(new MockMultipartFile("file", "cover.png", "image/png", PNG))
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.coverFileId").isNumber())
				.andExpect(jsonPath("$.coverFileName").value("cover.png"))
				.andExpect(jsonPath("$.prefaceFileId").value(nullValue()))
				.andReturn();

		long coverId = objectMapper.readTree(coverResult.getResponse().getContentAsString()).get("coverFileId").asLong();

		mockMvc.perform(get("/api/files/{id}", coverId)
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken))
				.andExpect(status().isOk())
				.andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("cover.png")));
	}

	@Test
	void adminCanAttachPrefaceAndBothAttachments() throws Exception {
		mockMvc.perform(multipart("/api/books/{id}/preface", book.getId())
						.file(new MockMultipartFile("file", "intro.pdf", "application/pdf", PDF))
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.prefaceFileId").isNumber())
				.andExpect(jsonPath("$.prefaceFileName").value("intro.pdf"));

		mockMvc.perform(multipart("/api/books/{id}/cover", book.getId())
						.file(new MockMultipartFile("file", "cover.jpg", "image/jpeg", JPEG))
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.coverFileName").value("cover.jpg"))
				.andExpect(jsonPath("$.prefaceFileName").value("intro.pdf"));
	}

	@Test
	void userCannotAttachOrRemoveFiles() throws Exception {
		mockMvc.perform(multipart("/api/books/{id}/cover", book.getId())
						.file(new MockMultipartFile("file", "cover.png", "image/png", PNG))
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken))
				.andExpect(status().isForbidden());
		mockMvc.perform(multipart("/api/books/{id}/preface", book.getId())
						.file(new MockMultipartFile("file", "intro.pdf", "application/pdf", PDF))
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken))
				.andExpect(status().isForbidden());
		mockMvc.perform(delete("/api/books/{id}/cover", book.getId())
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken))
				.andExpect(status().isForbidden());
		mockMvc.perform(multipart("/api/files")
						.file(new MockMultipartFile("file", "cover.png", "image/png", PNG))
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken))
				.andExpect(status().isForbidden());
	}

	@Test
	void invalidCoverAndPrefaceTypesAreRejected() throws Exception {
		mockMvc.perform(multipart("/api/books/{id}/cover", book.getId())
						.file(new MockMultipartFile("file", "intro.pdf", "application/pdf", PDF))
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("Cover must be a JPEG or PNG image"));

		mockMvc.perform(multipart("/api/books/{id}/preface", book.getId())
						.file(new MockMultipartFile("file", "cover.png", "image/png", PNG))
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("Preface must be a PDF document"));
	}

	@Test
	void adminCanRemoveCoverAndBookCrudStillWorks() throws Exception {
		mockMvc.perform(multipart("/api/books/{id}/cover", book.getId())
						.file(new MockMultipartFile("file", "cover.png", "image/png", PNG))
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
				.andExpect(status().isOk());

		mockMvc.perform(delete("/api/books/{id}/cover", book.getId())
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.coverFileId").value(nullValue()));

		mockMvc.perform(post("/api/user/books/{bookId}/borrow", book.getId())
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken))
				.andExpect(status().isNoContent());
		mockMvc.perform(post("/api/user/books/{bookId}/return", book.getId())
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + userToken))
				.andExpect(status().isNoContent());
	}

	private void ensureUser(String email, Role role) {
		User user = userRepository.findByEmail(email).orElseGet(User::new);
		user.setEmail(email);
		user.setFullName(email);
		user.setRole(role);
		user.setPassword(passwordEncoder.encode("User12345"));
		user = userRepository.save(user);
		if (memberRepository.findByEmail(email).isEmpty()) {
			Member member = new Member();
			member.setName(email);
			member.setEmail(email);
			member.setUser(user);
			memberRepository.save(member);
		}
	}
}
