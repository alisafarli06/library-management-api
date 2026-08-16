package com.library.security;

import com.library.entity.Member;
import com.library.entity.Role;
import com.library.entity.User;
import com.library.repository.MemberRepository;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UserProfileTest {

	private static final String EMAIL = "profile-user@library.com";
	private static final String PASSWORD = "Profile123!";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JwtService jwtService;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@BeforeEach
	void setUp() {
		User user = userRepository.findByEmail(EMAIL).orElseGet(User::new);
		user.setFullName("Profile User");
		user.setEmail(EMAIL);
		user.setPassword(passwordEncoder.encode(PASSWORD));
		user.setRole(Role.USER);
		User saved = userRepository.save(user);

		Member member = memberRepository.findByUser_Id(saved.getId()).orElseGet(Member::new);
		member.setName(saved.getFullName());
		member.setEmail(EMAIL);
		member.setUser(saved);
		memberRepository.save(member);
	}

	@Test
	void authenticatedUserCanReadOwnProfile() throws Exception {
		String accessToken = jwtService.generateAccessToken(EMAIL, Role.USER);

		mockMvc.perform(get("/api/user/profile")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value("Profile User"))
				.andExpect(jsonPath("$.email").value(EMAIL))
				.andExpect(jsonPath("$.role").doesNotExist())
				.andExpect(jsonPath("$.password").doesNotExist())
				.andExpect(jsonPath("$.id").doesNotExist());
	}

	@Test
	void authenticatedUserCanUpdateOwnName() throws Exception {
		String accessToken = jwtService.generateAccessToken(EMAIL, Role.USER);

		mockMvc.perform(patch("/api/user/profile")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "name": "  Ali Safarli  "
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value("Ali Safarli"))
				.andExpect(jsonPath("$.email").value(EMAIL));

		User updated = userRepository.findByEmail(EMAIL).orElseThrow();
		assertEquals("Ali Safarli", updated.getFullName());
		assertEquals(Role.USER, updated.getRole());
		assertEquals(EMAIL, updated.getEmail());

		Member member = memberRepository.findByUser_Id(updated.getId()).orElseThrow();
		assertEquals("Ali Safarli", member.getName());
	}

	@Test
	void blankNameIsRejected() throws Exception {
		String accessToken = jwtService.generateAccessToken(EMAIL, Role.USER);

		mockMvc.perform(patch("/api/user/profile")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "name": "   "
								}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400));
	}

	@Test
	void unauthenticatedProfileRequestIsRejected() throws Exception {
		mockMvc.perform(get("/api/user/profile").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isUnauthorized());

		mockMvc.perform(patch("/api/user/profile")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "name": "Someone Else"
								}
								"""))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void adminCanUpdateOwnProfileOnlyViaTokenIdentity() throws Exception {
		User admin = userRepository.findByEmail("admin-profile@library.com").orElseGet(User::new);
		admin.setFullName("Admin Profile");
		admin.setEmail("admin-profile@library.com");
		admin.setPassword(passwordEncoder.encode(PASSWORD));
		admin.setRole(Role.ADMIN);
		userRepository.save(admin);

		String accessToken = jwtService.generateAccessToken("admin-profile@library.com", Role.ADMIN);

		mockMvc.perform(patch("/api/user/profile")
						.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "name": "Admin Updated"
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value("Admin Updated"))
				.andExpect(jsonPath("$.email").value("admin-profile@library.com"));

		User other = userRepository.findByEmail(EMAIL).orElseThrow();
		assertEquals("Profile User", other.getFullName());
	}
}
