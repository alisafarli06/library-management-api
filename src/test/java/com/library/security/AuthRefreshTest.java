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

import java.util.UUID;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthRefreshTest {

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
		User user = userRepository.findByEmail("user@library.com").orElseGet(User::new);
		user.setFullName("Test User");
		user.setEmail("user@library.com");
		user.setPassword(passwordEncoder.encode("User12345"));
		user.setRole(Role.USER);
		userRepository.save(user);
	}

	@Test
	void registerReturnsAccessAndRefreshTokens() throws Exception {
		String email = "register-" + UUID.randomUUID() + "@library.com";

		mockMvc.perform(post("/api/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "fullName": "Register Test User",
								  "email": "%s",
								  "password": "Password123"
								}
								""".formatted(email)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.accessToken").isNotEmpty())
				.andExpect(jsonPath("$.refreshToken").isNotEmpty());
	}

	@Test
	void loginReturnsAccessAndRefreshTokens() throws Exception {
		mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "email": "user@library.com",
								  "password": "User12345"
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.accessToken").isNotEmpty())
				.andExpect(jsonPath("$.refreshToken").isNotEmpty());
	}

	@Test
	void refreshWithAccessTokenReturnsUnauthorized() throws Exception {
		String accessToken = jwtService.generateAccessToken("user@library.com", Role.USER);

		mockMvc.perform(post("/api/auth/refresh")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"refreshToken\":\"" + accessToken + "\"}"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.status").value(401));
	}

	@Test
	void refreshTokenCannotAccessProtectedEndpoint() throws Exception {
		String refreshToken = jwtService.generateRefreshToken("user@library.com", Role.USER);

		mockMvc.perform(get("/api/user/profile")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + refreshToken))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void refreshWithValidRefreshTokenReturnsNewTokenPair() throws Exception {
		String refreshToken = jwtService.generateRefreshToken("user@library.com", Role.USER);
		// JWT iat/exp are second-precision; wait so the rotated token differs.
		Thread.sleep(1100);

		mockMvc.perform(post("/api/auth/refresh")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"refreshToken\":\"" + refreshToken + "\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.accessToken").isNotEmpty())
				.andExpect(jsonPath("$.refreshToken").isNotEmpty())
				.andExpect(jsonPath("$.refreshToken").value(not(equalTo(refreshToken))));
	}

	@Test
	void refreshWithInvalidRefreshTokenReturnsUnauthorized() throws Exception {
		mockMvc.perform(post("/api/auth/refresh")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"refreshToken\":\"not-a-valid-jwt\"}"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.status").value(401))
				.andExpect(jsonPath("$.message").exists())
				.andExpect(jsonPath("$.timestamp").exists());
	}

	@Test
	void refreshWithExpiredRefreshTokenReturnsUnauthorized() throws Exception {
		String expiredRefreshToken = jwtService.generateRefreshToken("user@library.com", Role.USER, -1000L);

		mockMvc.perform(post("/api/auth/refresh")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"refreshToken\":\"" + expiredRefreshToken + "\"}"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.status").value(401))
				.andExpect(jsonPath("$.message").value("Refresh token expired"))
				.andExpect(jsonPath("$.timestamp").exists());
	}
}
