package com.library.security;

import com.library.entity.Role;
import com.library.entity.User;
import com.library.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RoleAuthorizationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JwtService jwtService;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@BeforeEach
	void setUp() {
		createUserIfMissing("user@library.com", "Test User", Role.USER, "User12345");
		createUserIfMissing("admin@library.com", "Library Admin", Role.ADMIN, "Admin123!");
	}

	@Test
	void userCannotAccessAdminDashboard() throws Exception {
		String token = jwtService.generateToken("user@library.com", Role.USER);

		mockMvc.perform(get("/api/admin/dashboard")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
				.andExpect(status().isForbidden());
	}

	@Test
	void adminCanAccessAdminDashboard() throws Exception {
		String token = jwtService.generateToken("admin@library.com", Role.ADMIN);

		mockMvc.perform(get("/api/admin/dashboard")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(content().string("Admin content"));
	}

	@Test
	void userCanAccessUserProfile() throws Exception {
		String token = jwtService.generateToken("user@library.com", Role.USER);

		mockMvc.perform(get("/api/user/profile")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(content().string("User content"));
	}

	@Test
	void userCanReadBooksAndAuthorsButNotMutateCatalogue() throws Exception {
		String token = jwtService.generateToken("user@library.com", Role.USER);

		mockMvc.perform(get("/api/books").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
				.andExpect(status().isOk());
		mockMvc.perform(get("/api/books/search").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
				.andExpect(status().isOk());
		mockMvc.perform(get("/api/books/1").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
				.andExpect(status().isNotFound());
		mockMvc.perform(get("/api/authors").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
				.andExpect(status().isOk());
		mockMvc.perform(get("/api/authors/1").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
				.andExpect(status().isNotFound());

		mockMvc.perform(post("/api/books")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{}"))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.status").value(403))
				.andExpect(jsonPath("$.message").value("Access denied"));
		mockMvc.perform(put("/api/books/1")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{}"))
				.andExpect(status().isForbidden());
		mockMvc.perform(delete("/api/books/1").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
				.andExpect(status().isForbidden());

		mockMvc.perform(post("/api/authors")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{}"))
				.andExpect(status().isForbidden());
		mockMvc.perform(put("/api/authors/1")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{}"))
				.andExpect(status().isForbidden());
		mockMvc.perform(delete("/api/authors/1").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
				.andExpect(status().isForbidden());
	}

	@Test
	void userCannotManageMembersButCanBorrow() throws Exception {
		String token = jwtService.generateToken("user@library.com", Role.USER);

		mockMvc.perform(get("/api/members").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
				.andExpect(status().isForbidden());
		mockMvc.perform(get("/api/members/1").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
				.andExpect(status().isForbidden());
		mockMvc.perform(post("/api/members")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{}"))
				.andExpect(status().isForbidden());
		mockMvc.perform(put("/api/members/1")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{}"))
				.andExpect(status().isForbidden());
		mockMvc.perform(delete("/api/members/1").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
				.andExpect(status().isForbidden());

		mockMvc.perform(post("/api/members/1/books/1/borrow")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
				.andExpect(status().isNotFound());
	}

	@Test
	void adminCanManageCatalogueMembersAndBorrow() throws Exception {
		String token = jwtService.generateToken("admin@library.com", Role.ADMIN);

		mockMvc.perform(post("/api/books")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{}"))
				.andExpect(status().isBadRequest());
		mockMvc.perform(put("/api/books/1")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{}"))
				.andExpect(status().isBadRequest());
		mockMvc.perform(delete("/api/books/1").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
				.andExpect(status().isNotFound());

		mockMvc.perform(post("/api/authors")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{}"))
				.andExpect(status().isBadRequest());
		mockMvc.perform(put("/api/authors/1")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{}"))
				.andExpect(status().isBadRequest());
		mockMvc.perform(delete("/api/authors/1").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
				.andExpect(status().isNotFound());

		mockMvc.perform(get("/api/members").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
				.andExpect(status().isOk());
		mockMvc.perform(post("/api/members")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{}"))
				.andExpect(status().isBadRequest());
		mockMvc.perform(put("/api/members/1")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{}"))
				.andExpect(status().isBadRequest());
		mockMvc.perform(delete("/api/members/1").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
				.andExpect(status().isNotFound());

		mockMvc.perform(post("/api/members/1/books/1/borrow")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
				.andExpect(status().isNotFound());
	}

	private void createUserIfMissing(String email, String fullName, Role role, String rawPassword) {
		if (userRepository.findByEmail(email).isEmpty()) {
			User user = new User();
			user.setFullName(fullName);
			user.setEmail(email);
			user.setPassword(passwordEncoder.encode(rawPassword));
			user.setRole(role);
			userRepository.save(user);
		} else {
			User existing = userRepository.findByEmail(email).orElseThrow();
			existing.setRole(role);
			existing.setPassword(passwordEncoder.encode(rawPassword));
			userRepository.save(existing);
		}
	}
}
