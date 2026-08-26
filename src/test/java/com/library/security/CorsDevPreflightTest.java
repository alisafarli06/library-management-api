package com.library.security;

import com.library.config.CorsProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Dev CORS: local Vite origins stay allowed even when FRONTEND_ORIGIN is set
 * (Render/Vercel env leaked into a local process).
 */
@SpringBootTest(properties = "FRONTEND_ORIGIN=https://library-management-web-4tu2-woad.vercel.app")
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class CorsDevPreflightTest {

	private static final String LOCALHOST = "http://localhost:5173";
	private static final String LOOPBACK = "http://127.0.0.1:5173";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private CorsProperties corsProperties;

	@Test
	void devProfileBindsLocalViteOrigins() {
		String origins = corsProperties.getAllowedOrigins();
		assertTrue(origins.contains(LOCALHOST), "resolved origins were: " + origins);
		assertTrue(origins.contains(LOOPBACK), "resolved origins were: " + origins);
	}

	@Test
	void optionsPreflightFromLocalhostIsAllowed() throws Exception {
		mockMvc.perform(options("/api/auth/login")
						.header(HttpHeaders.ORIGIN, LOCALHOST)
						.header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
						.header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "authorization,content-type"))
				.andExpect(status().isOk())
				.andExpect(content().string(not(containsString("Invalid CORS request"))))
				.andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, LOCALHOST))
				.andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, containsString("POST")))
				.andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, containsString("authorization")))
				.andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, containsString("content-type")));
	}

	@Test
	void optionsPreflightFromLoopbackIsAllowed() throws Exception {
		mockMvc.perform(options("/api/auth/login")
						.header(HttpHeaders.ORIGIN, LOOPBACK)
						.header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
						.header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "authorization,content-type"))
				.andExpect(status().isOk())
				.andExpect(content().string(not(containsString("Invalid CORS request"))))
				.andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, LOOPBACK));
	}

	@Test
	void preflightForProtectedRouteIsAllowedWithoutJwt() throws Exception {
		mockMvc.perform(options("/api/admin/users")
						.header(HttpHeaders.ORIGIN, LOCALHOST)
						.header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET")
						.header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "authorization,content-type"))
				.andExpect(status().isOk())
				.andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, LOCALHOST));
	}

	@Test
	void loginResponseIncludesCorsHeadersForAllowedOrigin() throws Exception {
		mockMvc.perform(post("/api/auth/login")
						.header(HttpHeaders.ORIGIN, LOCALHOST)
						.header(HttpHeaders.CONTENT_TYPE, "application/json")
						.content("""
								{"email":"nobody@library.com","password":"Password123"}
								"""))
				.andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, LOCALHOST));
	}

	@Test
	void optionsPreflightFromUnknownOriginIsRejected() throws Exception {
		mockMvc.perform(options("/api/auth/login")
						.header(HttpHeaders.ORIGIN, "https://evil.example")
						.header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
						.header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "authorization,content-type"))
				.andExpect(status().isForbidden())
				.andExpect(content().string(containsString("Invalid CORS request")));
	}
}
