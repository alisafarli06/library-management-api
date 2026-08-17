package com.library.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "app.cors.allowed-origins=https://library-management-web-4tu2-woad.vercel.app")
@AutoConfigureMockMvc
class CorsProdOriginSecurityTest {

	private static final String VERCEL_ORIGIN = "https://library-management-web-4tu2-woad.vercel.app";

	@Autowired
	private MockMvc mockMvc;

	@Test
	void preflightFromVercelOriginIsAllowedWithoutJwt() throws Exception {
		mockMvc.perform(options("/api/auth/login")
						.header(HttpHeaders.ORIGIN, VERCEL_ORIGIN)
						.header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
						.header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "authorization,content-type"))
				.andExpect(status().isOk())
				.andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, VERCEL_ORIGIN));
	}

	@Test
	void unknownOriginIsRejectedWhenProdOriginIsConfigured() throws Exception {
		mockMvc.perform(options("/api/auth/login")
						.header(HttpHeaders.ORIGIN, "https://evil.example")
						.header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST"))
				.andExpect(status().isForbidden());
	}
}
