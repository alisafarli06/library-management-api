package com.library.controller;

import com.library.entity.Role;
import com.library.entity.User;
import com.library.repository.FileMetadataRepository;
import com.library.repository.UserRepository;
import com.library.security.JwtService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class FileControllerTest {

	@TempDir
	static Path tempDir;

	@DynamicPropertySource
	static void registerStorageDirectory(DynamicPropertyRegistry registry) {
		registry.add("app.file.storage-directory", () -> tempDir.toString());
		registry.add("app.file.max-size", () -> "10485760");
	}

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JwtService jwtService;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private FileMetadataRepository fileMetadataRepository;

	private final ObjectMapper objectMapper = new ObjectMapper();

	private String token;
	private String adminToken;

	@BeforeEach
	void setUp() {
		fileMetadataRepository.deleteAll();
		User user = userRepository.findByEmail("file-user@library.com").orElseGet(User::new);
		user.setEmail("file-user@library.com");
		user.setFullName("File User");
		user.setRole(Role.USER);
		user.setPassword(passwordEncoder.encode("User12345"));
		userRepository.save(user);
		token = jwtService.generateToken("file-user@library.com", Role.USER);

		User admin = userRepository.findByEmail("file-admin@library.com").orElseGet(User::new);
		admin.setEmail("file-admin@library.com");
		admin.setFullName("File Admin");
		admin.setRole(Role.ADMIN);
		admin.setPassword(passwordEncoder.encode("Admin12345"));
		userRepository.save(admin);
		adminToken = jwtService.generateToken("file-admin@library.com", Role.ADMIN);
	}

	@Test
	void upload_successfulMultipartUpload() throws Exception {
		byte[] pdf = "%PDF-1.4 hello".getBytes();
		MockMultipartFile file = new MockMultipartFile("file", "notes.pdf", "application/pdf", pdf);

		MvcResult result = mockMvc.perform(multipart("/api/files")
						.file(file)
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").isNumber())
				.andExpect(jsonPath("$.originalFilename").value("notes.pdf"))
				.andExpect(jsonPath("$.contentType").value("application/pdf"))
				.andExpect(jsonPath("$.size").value(pdf.length))
				.andExpect(jsonPath("$.storedFilename").doesNotExist())
				.andReturn();

		String responseBody = result.getResponse().getContentAsString();
		assertFalse(responseBody.contains(tempDir.toString()));
		assertTrue(Files.list(tempDir).findAny().isPresent());
	}

	@Test
	void upload_rejectsEmptyFile() throws Exception {
		MockMultipartFile file = new MockMultipartFile("file", "empty.pdf", "application/pdf", new byte[0]);

		mockMvc.perform(multipart("/api/files")
						.file(file)
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.message").value("File must not be empty"));
	}

	@Test
	void upload_rejectsUnsupportedType() throws Exception {
		MockMultipartFile file = new MockMultipartFile(
				"file",
				"script.sh",
				"application/x-sh",
				"#!/bin/sh\necho hi".getBytes()
		);

		mockMvc.perform(multipart("/api/files")
						.file(file)
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400));
	}

	@Test
	void download_returnsFileWithHeaders() throws Exception {
		byte[] png = new byte[] {
				(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0x00, 0x01
		};
		MockMultipartFile file = new MockMultipartFile("file", "cover.png", "image/png", png);

		MvcResult uploadResult = mockMvc.perform(multipart("/api/files")
						.file(file)
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken))
				.andExpect(status().isCreated())
				.andReturn();

		JsonNode uploadJson = objectMapper.readTree(uploadResult.getResponse().getContentAsString());
		long id = uploadJson.get("id").asLong();

		mockMvc.perform(get("/api/files/{id}", id)
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_PNG_VALUE))
				.andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, containsString("cover.png")))
				.andExpect(header().doesNotExist("X-Storage-Path"));
	}

	@Test
	void download_nonexistentFileReturns404() throws Exception {
		mockMvc.perform(get("/api/files/{id}", 9_999_999L)
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404))
				.andExpect(jsonPath("$.message").value("File not found with id: 9999999"));
	}
}
