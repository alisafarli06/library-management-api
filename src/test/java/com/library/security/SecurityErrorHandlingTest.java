package com.library.security;

import com.library.entity.Role;
import com.library.entity.User;
import com.library.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityErrorHandlingTest {

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
		ensureUser("user@library.com", "Test User", Role.USER, "User12345");
		ensureUser("admin@library.com", "Library Admin", Role.ADMIN, "Admin123!");
	}

	@Test
	void protectedEndpointWithoutTokenReturnsUnauthorized() throws Exception {
		mockMvc.perform(get("/api/user/profile")
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.status").value(401))
				.andExpect(jsonPath("$.message").value("Unauthorized"))
				.andExpect(jsonPath("$.timestamp").exists());
	}

	@Test
	void swaggerUiIsAccessibleWithoutToken() throws Exception {
		mockMvc.perform(get("/swagger-ui/index.html"))
				.andExpect(status().isOk());
	}

	@Test
	void openApiDocsAreAccessibleWithoutToken() throws Exception {
		mockMvc.perform(get("/v3/api-docs").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.openapi").exists())
				.andExpect(jsonPath("$.info.title").value("Library Management API"));
	}

	@Test
	void protectedApiStillRequiresAuthentication() throws Exception {
		mockMvc.perform(get("/api/books").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void expiredTokenReturnsUnauthorized() throws Exception {
		String expiredToken = jwtService.generateToken("user@library.com", Role.USER, -1000L);

		mockMvc.perform(get("/api/user/profile")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + expiredToken)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.status").value(401))
				.andExpect(jsonPath("$.message").value("Token expired"))
				.andExpect(jsonPath("$.timestamp").exists());
	}

	@Test
	void validTokenStillWorks() throws Exception {
		String token = jwtService.generateToken("user@library.com", Role.USER);

		mockMvc.perform(get("/api/user/profile")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.email").value("user@library.com"))
				.andExpect(jsonPath("$.name").value("Test User"));
	}

	@Test
	void userAccessingAdminEndpointReturnsForbidden() throws Exception {
		String token = jwtService.generateToken("user@library.com", Role.USER);

		mockMvc.perform(get("/api/admin/dashboard")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.status").value(403))
				.andExpect(jsonPath("$.message").value("Access denied"))
				.andExpect(jsonPath("$.timestamp").exists());
	}

	@Test
	void adminAccessingAdminEndpointReturnsOk() throws Exception {
		String token = jwtService.generateToken("admin@library.com", Role.ADMIN);

		mockMvc.perform(get("/api/admin/dashboard")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(content().string("Admin content"));
	}

	private void ensureUser(String email, String fullName, Role role, String rawPassword) {
		User user = userRepository.findByEmail(email).orElseGet(User::new);
		user.setFullName(fullName);
		user.setEmail(email);
		user.setPassword(passwordEncoder.encode(rawPassword));
		user.setRole(role);
		userRepository.save(user);
	}
}
