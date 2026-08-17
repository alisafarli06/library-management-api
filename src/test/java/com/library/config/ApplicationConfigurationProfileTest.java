package com.library.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApplicationConfigurationProfileTest {

	@Test
	void prodYaml_doesNotContainHardcodedSecretsOrUnsafePasswordDefaults() throws IOException {
		String prodYaml = readClasspathResource("/application-prod.yml");

		assertFalse(prodYaml.contains("CHANGE_ME"));
		assertFalse(prodYaml.contains("password: postgres"));
		assertTrue(prodYaml.contains("${DB_PASSWORD}"));
		assertTrue(prodYaml.contains("${JWT_SECRET}"));
		assertTrue(prodYaml.contains("${DB_URL}"));
		assertTrue(prodYaml.contains("${DB_USERNAME}"));
		assertTrue(prodYaml.contains("${FILE_STORAGE_DIRECTORY}"));
		assertTrue(prodYaml.contains("${ADMIN_INITIAL_PASSWORD:${ADMIN_PASSWORD}}"));
		assertTrue(prodYaml.contains("${ADMIN_PASSWORD}"));
		assertFalse(prodYaml.contains("ADMIN_PASSWORD:"));
		assertFalse(prodYaml.contains("Admin123!"));
		assertFalse(prodYaml.contains("CHANGE_ME_ADMIN_PASSWORD"));
	}

	@Test
	void baseAndDevYaml_preserveExistingEnvironmentVariableNames() throws IOException {
		String baseYaml = readClasspathResource("/application.yml");
		String devYaml = readClasspathResource("/application-dev.yml");

		assertTrue(baseYaml.contains("active: ${SPRING_PROFILES_ACTIVE:dev}"));
		assertTrue(devYaml.contains("${DB_URL"));
		assertTrue(devYaml.contains("${DB_USERNAME"));
		assertTrue(devYaml.contains("${DB_PASSWORD"));
		assertTrue(baseYaml.contains("JWT_SECRET"));
		assertTrue(baseYaml.contains("FILE_STORAGE_DIRECTORY"));
		assertTrue(baseYaml.contains("CACHE_BOOKS_MAXIMUM_SIZE"));
		assertTrue(baseYaml.contains("FILE_CLEANUP_CRON"));
		assertTrue(baseYaml.contains("ASYNC_NOTIFICATION_DELAY_MS"));
		assertTrue(baseYaml.contains("ADMIN_INITIAL_PASSWORD"));
		assertTrue(baseYaml.contains("ADMIN_PASSWORD"));
		assertTrue(devYaml.contains("ADMIN_INITIAL_PASSWORD"));
		assertTrue(devYaml.contains("ADMIN_PASSWORD"));
	}

	@Test
	void propertyOverridesWinOverYamlDefaults() throws IOException {
		StandardEnvironment environment = new StandardEnvironment();
		environment.setActiveProfiles("dev");

		YamlPropertySourceLoader loader = new YamlPropertySourceLoader();
		addYaml(environment, loader, "application.yml");
		addYaml(environment, loader, "application-dev.yml");

		environment.getPropertySources().addFirst(new MapPropertySource(
				"testOverrides",
				Map.of(
						"app.async.notification.delay-ms", "1234",
						"app.cache.books.maximum-size", "42"
				)
		));

		assertTrue(List.of(environment.getActiveProfiles()).contains("dev"));
		assertEquals("library-management-api", environment.getProperty("spring.application.name"));
		assertEquals("1234", environment.getProperty("app.async.notification.delay-ms"));
		assertEquals("42", environment.getProperty("app.cache.books.maximum-size"));
	}

	private static void addYaml(
			StandardEnvironment environment,
			YamlPropertySourceLoader loader,
			String classpathLocation) throws IOException {
		ClassPathResource resource = new ClassPathResource(classpathLocation);
		loader.load(classpathLocation, resource).forEach(source ->
				environment.getPropertySources().addLast(source)
		);
	}

	private static String readClasspathResource(String path) throws IOException {
		try (var input = ApplicationConfigurationProfileTest.class.getResourceAsStream(path)) {
			return new String(Objects.requireNonNull(input, path).readAllBytes(), StandardCharsets.UTF_8);
		}
	}
}
