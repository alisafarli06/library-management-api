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
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthChangePasswordTest {

	private static final String EMAIL = "change-password@library.com";
	private static final String OLD_PASSWORD = "OldPass123";
	private static final String NEW_PASSWORD = "NewPass456";

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
		User user = userRepository.findByEmail(EMAIL).orElseGet(User::new);
		user.setFullName("Change Password User");
		user.setEmail(EMAIL);
		user.setPassword(passwordEncoder.encode(OLD_PASSWORD));
		user.setRole(Role.USER);
		userRepository.save(user);
	}

	@Test
	void authenticatedUserCanChangePassword() throws Exception {
		String accessToken = jwtService.generateAccessToken(EMAIL, Role.USER);

		mockMvc.perform(post("/api/auth/change-password")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "currentPassword": "%s",
								  "newPassword": "%s"
								}
								""".formatted(OLD_PASSWORD, NEW_PASSWORD)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.message").value("Password changed successfully."));

		User updated = userRepository.findByEmail(EMAIL).orElseThrow();
		assertTrue(passwordEncoder.matches(NEW_PASSWORD, updated.getPassword()));
	}

	@Test
	void incorrectCurrentPasswordIsRejected() throws Exception {
		String accessToken = jwtService.generateAccessToken(EMAIL, Role.USER);

		mockMvc.perform(post("/api/auth/change-password")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "currentPassword": "WrongPass99",
								  "newPassword": "%s"
								}
								""".formatted(NEW_PASSWORD)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("Current password is incorrect"));
	}

	@Test
	void invalidNewPasswordIsRejected() throws Exception {
		String accessToken = jwtService.generateAccessToken(EMAIL, Role.USER);

		mockMvc.perform(post("/api/auth/change-password")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "currentPassword": "%s",
								  "newPassword": "short"
								}
								""".formatted(OLD_PASSWORD)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("Validation failed"))
				.andExpect(jsonPath("$.fieldErrors.newPassword").exists());
	}

	@Test
	void successfulPasswordUpdateAllowsNewPasswordLoginAndRejectsOld() throws Exception {
		String accessToken = jwtService.generateAccessToken(EMAIL, Role.USER);

		mockMvc.perform(post("/api/auth/change-password")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "currentPassword": "%s",
								  "newPassword": "%s"
								}
								""".formatted(OLD_PASSWORD, NEW_PASSWORD)))
				.andExpect(status().isOk());

		mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "email": "%s",
								  "password": "%s"
								}
								""".formatted(EMAIL, OLD_PASSWORD)))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.message").value("Invalid email or password"));

		mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "email": "%s",
								  "password": "%s"
								}
								""".formatted(EMAIL, NEW_PASSWORD)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.accessToken").isNotEmpty());
	}

	@Test
	void unauthenticatedRequestIsRejected() throws Exception {
		mockMvc.perform(post("/api/auth/change-password")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "currentPassword": "%s",
								  "newPassword": "%s"
								}
								""".formatted(OLD_PASSWORD, NEW_PASSWORD)))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void adminCanAlsoChangeOwnPassword() throws Exception {
		User admin = userRepository.findByEmail("admin-change@library.com").orElseGet(User::new);
		admin.setFullName("Admin Change");
		admin.setEmail("admin-change@library.com");
		admin.setPassword(passwordEncoder.encode(OLD_PASSWORD));
		admin.setRole(Role.ADMIN);
		userRepository.save(admin);

		String accessToken = jwtService.generateAccessToken("admin-change@library.com", Role.ADMIN);

		mockMvc.perform(post("/api/auth/change-password")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "currentPassword": "%s",
								  "newPassword": "%s"
								}
								""".formatted(OLD_PASSWORD, NEW_PASSWORD)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.message").value("Password changed successfully."));
	}
}
