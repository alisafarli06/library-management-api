package com.library.config;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsConfig {

	@Bean
	public CorsConfigurationSource corsConfigurationSource(
			@Value("${app.cors.allowed-origins:}") String allowedOrigins) {
		CorsConfiguration configuration = new CorsConfiguration();

		if (!allowedOrigins.isBlank()) {
			Arrays.stream(allowedOrigins.split(","))
					.map(String::trim)
					.filter(origin -> !origin.isEmpty())
					.forEach(configuration::addAllowedOriginPattern);
			configuration.addAllowedMethod("*");
			configuration.addAllowedHeader("*");
			configuration.setAllowCredentials(true);
		}

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		if (!configuration.getAllowedOriginPatterns().isEmpty()) {
			source.registerCorsConfiguration("/api/**", configuration);
		}
		return source;
	}
}
