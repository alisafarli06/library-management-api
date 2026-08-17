package com.library.config;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.util.StringUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsConfig {

	private static final Logger log = LoggerFactory.getLogger(CorsConfig.class);

	static final List<String> ALLOWED_METHODS = List.of(
			HttpMethod.GET.name(),
			HttpMethod.POST.name(),
			HttpMethod.PUT.name(),
			HttpMethod.PATCH.name(),
			HttpMethod.DELETE.name(),
			HttpMethod.OPTIONS.name(),
			HttpMethod.HEAD.name()
	);

	static final List<String> ALLOWED_HEADERS = List.of(
			HttpHeaders.AUTHORIZATION,
			HttpHeaders.CONTENT_TYPE,
			HttpHeaders.ACCEPT,
			HttpHeaders.ORIGIN,
			HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD,
			HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS
	);

	static final List<String> EXPOSED_HEADERS = List.of(
			HttpHeaders.AUTHORIZATION,
			HttpHeaders.CONTENT_DISPOSITION
	);

	@Bean
	public CorsConfigurationSource corsConfigurationSource(CorsProperties corsProperties) {
		List<String> origins = parseOrigins(corsProperties.getAllowedOrigins());
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedMethods(ALLOWED_METHODS);
		configuration.setAllowedHeaders(ALLOWED_HEADERS);
		configuration.setExposedHeaders(EXPOSED_HEADERS);
		configuration.setMaxAge(3600L);
		configuration.setAllowCredentials(false);

		applyOrigins(configuration, origins);

		List<String> allowedOrigins = configuration.getAllowedOrigins();
		List<String> allowedPatterns = configuration.getAllowedOriginPatterns();
		if ((allowedOrigins == null || allowedOrigins.isEmpty())
				&& (allowedPatterns == null || allowedPatterns.isEmpty())) {
			log.warn("CORS allowed origins are empty; browser clients on other hosts will fail. "
					+ "Set FRONTEND_ORIGIN (or CORS_ALLOWED_ORIGINS) to the Vercel app origin.");
		} else {
			log.info("CORS allowed origins: {}", origins);
		}

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}

	static List<String> parseOrigins(String raw) {
		List<String> origins = new ArrayList<>();
		if (!StringUtils.hasText(raw)) {
			return origins;
		}
		for (String part : raw.split(",")) {
			String origin = part.trim().replaceAll("/+$", "");
			if (StringUtils.hasText(origin)) {
				origins.add(origin);
			}
		}
		return origins;
	}

	static void applyOrigins(CorsConfiguration configuration, List<String> origins) {
		for (String origin : origins) {
			if ("*".equals(origin)) {
				configuration.addAllowedOriginPattern("*");
				continue;
			}
			if (origin.contains("*")) {
				configuration.addAllowedOriginPattern(origin);
			} else {
				configuration.addAllowedOrigin(origin);
			}
		}
	}
}
