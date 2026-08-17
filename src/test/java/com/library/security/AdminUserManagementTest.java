package com.library.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.config.AdminUserInitializer;
import com.library.config.AdminUserProperties;
import com.library.entity.AccountStatus;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AdminUserManagementTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JwtService jwtService;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private AdminUserInitializer adminUserInitializer;

	@Autowired
	private AdminUserProperties adminUserProperties;

	private final ObjectMapper objectMapper = new ObjectMapper();

	@BeforeEach
	void setUp() {
		createUserIfMissing("user@library.com", "Test User", Role.USER, "User12345");
		createUserIfMissing("second-admin@library.com", "Second Admin", Role.ADMIN, "Admin123!");
		createUserIfMissing(
				adminUserProperties.getEmail(),
				adminUserProperties.getFullName(),
				Role.ADMIN,
				adminUserProperties.getPassword()
		);
	}

	@Test
	void bootstrapAdminCanLoginWithConfiguredPassword() throws Exception {
		String email = adminUserProperties.getEmail();
		String password = adminUserProperties.getPassword();

		MvcResult result = mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "email": "%s",
								  "password": "%s"
								}
								""".formatted(email, password)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.accessToken").isNotEmpty())
				.andExpect(jsonPath("$.refreshToken").isNotEmpty())
				.andExpect(jsonPath("$.password").doesNotExist())
				.andReturn();

		String accessToken = objectMapper.readTree(result.getResponse().getContentAsString())
				.get("accessToken")
				.asText();
		assertEquals("ADMIN", jwtService.extractRole(accessToken));
	}

	@Test
	void userCannotAccessAdminUserEndpoints() throws Exception {
		String token = jwtService.generateToken("user@library.com", Role.USER);
		User target = userRepository.findByEmail("user@library.com").orElseThrow();

		mockMvc.perform(get("/api/admin/users")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.message").value("Access denied"));
		mockMvc.perform(get("/api/admin/users/" + target.getId())
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
				.andExpect(status().isForbidden());
		mockMvc.perform(patch("/api/admin/users/" + target.getId() + "/role")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{ "role": "ADMIN" }
								"""))
				.andExpect(status().isForbidden());
		mockMvc.perform(delete("/api/admin/users/" + target.getId())
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
				.andExpect(status().isForbidden());
		mockMvc.perform(patch("/api/admin/users/" + target.getId() + "/status")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{ "blocked": true }
								"""))
				.andExpect(status().isForbidden());
	}

	@Test
	void unauthenticatedUserCannotAccessAdminUsers() throws Exception {
		mockMvc.perform(get("/api/admin/users"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void adminCanListUsersWithoutPasswordHashes() throws Exception {
		String token = jwtService.generateToken(adminUserProperties.getEmail(), Role.ADMIN);

		mockMvc.perform(get("/api/admin/users")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
						.param("q", "user@library.com"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content[*].email", hasItem("user@library.com")))
				.andExpect(jsonPath("$.content[*].role", hasItem("USER")))
				.andExpect(jsonPath("$.content[0].password").doesNotExist());
	}

	@Test
	void adminCanGetUserById() throws Exception {
		String token = jwtService.generateToken(adminUserProperties.getEmail(), Role.ADMIN);
		User target = userRepository.findByEmail("user@library.com").orElseThrow();

		mockMvc.perform(get("/api/admin/users/" + target.getId())
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(target.getId()))
				.andExpect(jsonPath("$.email").value("user@library.com"))
				.andExpect(jsonPath("$.role").value("USER"))
				.andExpect(jsonPath("$.status").value("ACTIVE"))
				.andExpect(jsonPath("$.password").doesNotExist());
	}

	@Test
	void adminCanPromoteUserToAdmin() throws Exception {
		String token = jwtService.generateToken(adminUserProperties.getEmail(), Role.ADMIN);
		User target = userRepository.findByEmail("user@library.com").orElseThrow();

		mockMvc.perform(patch("/api/admin/users/" + target.getId() + "/role")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{ "role": "ADMIN" }
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.role").value("ADMIN"))
				.andExpect(jsonPath("$.password").doesNotExist());

		assertEquals(Role.ADMIN, userRepository.findById(target.getId()).orElseThrow().getRole());
	}

	@Test
	void adminCanDemoteAdminToUser() throws Exception {
		String token = jwtService.generateToken(adminUserProperties.getEmail(), Role.ADMIN);
		User target = userRepository.findByEmail("second-admin@library.com").orElseThrow();

		mockMvc.perform(patch("/api/admin/users/" + target.getId() + "/role")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{ "role": "USER" }
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.role").value("USER"));

		assertEquals(Role.USER, userRepository.findById(target.getId()).orElseThrow().getRole());
	}

	@Test
	void demotedAdminExistingAccessTokenCannotCallAdminEndpoints() throws Exception {
		String actorToken = jwtService.generateToken(adminUserProperties.getEmail(), Role.ADMIN);
		User target = userRepository.findByEmail("second-admin@library.com").orElseThrow();
		String targetAccessToken = jwtService.generateToken(target.getEmail(), Role.ADMIN);

		mockMvc.perform(patch("/api/admin/users/" + target.getId() + "/role")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + actorToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{ "role": "USER" }
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.role").value("USER"));

		mockMvc.perform(get("/api/admin/users")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + targetAccessToken))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.message").value("Access denied"));
	}

	@Test
	void demotedAdminRefreshIssuesUserTokens() throws Exception {
		String actorToken = jwtService.generateToken(adminUserProperties.getEmail(), Role.ADMIN);
		User target = userRepository.findByEmail("second-admin@library.com").orElseThrow();
		String staleRefreshToken = jwtService.generateRefreshToken(target.getEmail(), Role.ADMIN);

		mockMvc.perform(patch("/api/admin/users/" + target.getId() + "/role")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + actorToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{ "role": "USER" }
								"""))
				.andExpect(status().isOk());

		var result = mockMvc.perform(post("/api/auth/refresh")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"refreshToken\":\"" + staleRefreshToken + "\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.accessToken").isNotEmpty())
				.andReturn();

		String newAccessToken = objectMapper.readTree(result.getResponse().getContentAsString())
				.get("accessToken")
				.asText();
		assertEquals("USER", jwtService.extractRole(newAccessToken));

		mockMvc.perform(get("/api/admin/users")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + newAccessToken))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.message").value("Access denied"));
	}

	@Test
	void lastAdminCannotBeDowngraded() throws Exception {
		User lastAdmin = userRepository.findByEmail(adminUserProperties.getEmail()).orElseThrow();
		userRepository.findAll().stream()
				.filter(user -> user.getRole() == Role.ADMIN)
				.filter(user -> !user.getId().equals(lastAdmin.getId()))
				.forEach(user -> {
					user.setRole(Role.USER);
					userRepository.save(user);
				});
		String token = jwtService.generateToken(adminUserProperties.getEmail(), Role.ADMIN);

		mockMvc.perform(patch("/api/admin/users/" + lastAdmin.getId() + "/role")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{ "role": "USER" }
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("You cannot change your own role"));
	}

	@Test
	void missingUserReturns404() throws Exception {
		String token = jwtService.generateToken(adminUserProperties.getEmail(), Role.ADMIN);

		mockMvc.perform(get("/api/admin/users/999999")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
				.andExpect(status().isNotFound());
	}

	@Test
	void runningInitializerAgainDoesNotCreateDuplicateAdmin() {
		String email = adminUserProperties.getEmail();
		long before = userRepository.findAll().stream().filter(user -> email.equalsIgnoreCase(user.getEmail())).count();

		adminUserInitializer.run();
		adminUserInitializer.run();

		long after = userRepository.findAll().stream().filter(user -> email.equalsIgnoreCase(user.getEmail())).count();
		assertEquals(1, before);
		assertEquals(1, after);
		assertEquals(Role.ADMIN, userRepository.findByEmail(email).orElseThrow().getRole());
	}

	@Test
	void registerStillCreatesUserRoleOnly() throws Exception {
		String email = "register-role-" + UUID.randomUUID() + "@library.com";

		mockMvc.perform(post("/api/auth/register")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "fullName": "New Member",
								  "email": "%s",
								  "password": "Password123"
								}
								""".formatted(email)))
				.andExpect(status().isCreated());

		assertEquals(Role.USER, userRepository.findByEmail(email).orElseThrow().getRole());
	}

	@Test
	void adminCannotChangeOwnRole() throws Exception {
		User admin = userRepository.findByEmail(adminUserProperties.getEmail()).orElseThrow();
		String token = jwtService.generateToken(admin.getEmail(), Role.ADMIN);

		mockMvc.perform(patch("/api/admin/users/" + admin.getId() + "/role")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{ "role": "USER" }
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("You cannot change your own role"));
	}

	@Test
	void adminCanBlockAndUnblockUser() throws Exception {
		String token = jwtService.generateToken(adminUserProperties.getEmail(), Role.ADMIN);
		User target = userRepository.findByEmail("user@library.com").orElseThrow();

		mockMvc.perform(patch("/api/admin/users/" + target.getId() + "/status")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{ "blocked": true }
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("BLOCKED"));

		mockMvc.perform(patch("/api/admin/users/" + target.getId() + "/status")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{ "blocked": false }
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("ACTIVE"));
	}

	@Test
	void adminCannotBlockThemselves() throws Exception {
		User admin = userRepository.findByEmail(adminUserProperties.getEmail()).orElseThrow();
		String token = jwtService.generateToken(admin.getEmail(), Role.ADMIN);

		mockMvc.perform(patch("/api/admin/users/" + admin.getId() + "/status")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{ "blocked": true }
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("You cannot block or unblock your own account"));
	}

	@Test
	void lastAdminCannotBeBlocked() throws Exception {
		User lastAdmin = userRepository.findByEmail(adminUserProperties.getEmail()).orElseThrow();
		userRepository.findAll().stream()
				.filter(user -> user.getRole() == Role.ADMIN)
				.filter(user -> !user.getId().equals(lastAdmin.getId()))
				.forEach(user -> {
					user.setRole(Role.USER);
					userRepository.save(user);
				});
		String token = jwtService.generateToken(adminUserProperties.getEmail(), Role.ADMIN);

		mockMvc.perform(patch("/api/admin/users/" + lastAdmin.getId() + "/status")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{ "blocked": true }
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("You cannot block or unblock your own account"));
	}

	@Test
	void blockedUserCannotLogIn() throws Exception {
		User target = userRepository.findByEmail("user@library.com").orElseThrow();
		target.setStatus(AccountStatus.BLOCKED);
		userRepository.save(target);

		mockMvc.perform(post("/api/auth/login")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "email": "user@library.com",
								  "password": "User12345"
								}
								"""))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.message").value("Account is blocked"));
	}

	@Test
	void blockedUserJwtIsRejected() throws Exception {
		User target = userRepository.findByEmail("user@library.com").orElseThrow();
		String token = jwtService.generateToken(target.getEmail(), Role.USER);
		target.setStatus(AccountStatus.BLOCKED);
		userRepository.save(target);

		mockMvc.perform(get("/api/user/profile")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.message").value("Account is blocked"));
	}

	@Test
	void blockedUserCannotRefresh() throws Exception {
		User target = userRepository.findByEmail("user@library.com").orElseThrow();
		String refreshToken = jwtService.generateRefreshToken(target.getEmail(), Role.USER);
		target.setStatus(AccountStatus.BLOCKED);
		userRepository.save(target);

		mockMvc.perform(post("/api/auth/refresh")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"refreshToken\":\"" + refreshToken + "\"}"))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.message").value("Account is blocked"));
	}

	private void createUserIfMissing(String email, String fullName, Role role, String rawPassword) {
		User user = userRepository.findByEmail(email).orElseGet(User::new);
		user.setFullName(fullName);
		user.setEmail(email);
		user.setPassword(passwordEncoder.encode(rawPassword));
		user.setRole(role);
		user.setStatus(AccountStatus.ACTIVE);
		userRepository.save(user);
	}
}
