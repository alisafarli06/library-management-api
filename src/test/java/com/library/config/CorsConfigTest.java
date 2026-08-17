package com.library.config;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CorsConfigTest {

	private static final String VERCEL_ORIGIN = "https://library-management-web-4tu2-woad.vercel.app";

	@Test
	void parseOrigins_stripsWhitespaceAndTrailingSlashes() {
		List<String> origins = CorsConfig.parseOrigins(
				" https://app.vercel.app/ , http://localhost:5173/ "
		);

		assertEquals(List.of("https://app.vercel.app", "http://localhost:5173"), origins);
	}

	@Test
	void allowsLocalhostOriginsAndRejectsUnknownOrigin() {
		CorsProperties properties = new CorsProperties();
		properties.setAllowedOrigins("http://localhost:5173,http://127.0.0.1:5173");
		CorsConfigurationSource source = new CorsConfig().corsConfigurationSource(properties);

		MockHttpServletRequest request = new MockHttpServletRequest("OPTIONS", "/api/auth/login");
		request.addHeader(HttpHeaders.ORIGIN, "http://localhost:5173");
		CorsConfiguration configuration = source.getCorsConfiguration(request);

		assertNotNull(configuration);
		assertEquals("http://localhost:5173", configuration.checkOrigin("http://localhost:5173"));
		assertEquals("http://127.0.0.1:5173", configuration.checkOrigin("http://127.0.0.1:5173"));
		assertNull(configuration.checkOrigin("https://evil.example"));
		assertFalse(Boolean.TRUE.equals(configuration.getAllowCredentials()));
		assertNotNull(configuration.checkHeaders(List.of(HttpHeaders.AUTHORIZATION, HttpHeaders.CONTENT_TYPE)));
	}

	@Test
	void allowsVercelOriginFromProductionConfiguration() {
		CorsProperties properties = new CorsProperties();
		properties.setAllowedOrigins(VERCEL_ORIGIN);
		CorsConfigurationSource source = new CorsConfig().corsConfigurationSource(properties);

		MockHttpServletRequest request = new MockHttpServletRequest("OPTIONS", "/api/auth/login");
		request.addHeader(HttpHeaders.ORIGIN, VERCEL_ORIGIN);
		CorsConfiguration configuration = source.getCorsConfiguration(request);

		assertNotNull(configuration);
		assertEquals(VERCEL_ORIGIN, configuration.checkOrigin(VERCEL_ORIGIN));
		assertNull(configuration.checkOrigin("http://localhost:5173"));
		assertNull(configuration.checkOrigin("https://evil.example"));
		assertFalse(Boolean.TRUE.equals(configuration.getAllowCredentials()));
		assertNotNull(configuration.checkHttpMethod(HttpMethod.GET));
		assertNotNull(configuration.checkHttpMethod(HttpMethod.POST));
		assertNotNull(configuration.checkHttpMethod(HttpMethod.PUT));
		assertNotNull(configuration.checkHttpMethod(HttpMethod.PATCH));
		assertNotNull(configuration.checkHttpMethod(HttpMethod.DELETE));
		assertNotNull(configuration.checkHttpMethod(HttpMethod.OPTIONS));
		assertNotNull(configuration.checkHeaders(List.of(HttpHeaders.AUTHORIZATION, HttpHeaders.CONTENT_TYPE)));
	}

	@Test
	void wildcardOriginDoesNotEnableCredentials() {
		CorsProperties properties = new CorsProperties();
		properties.setAllowedOrigins("*");
		CorsConfigurationSource source = new CorsConfig().corsConfigurationSource(properties);
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/auth/login");
		CorsConfiguration configuration = source.getCorsConfiguration(request);

		assertNotNull(configuration);
		assertTrue(configuration.getAllowCredentials() == null || Boolean.FALSE.equals(configuration.getAllowCredentials()));
		assertEquals(
				"https://library-management-web-4tu2-woad.vercel.app",
				configuration.checkOrigin("https://library-management-web-4tu2-woad.vercel.app")
		);
	}
}
