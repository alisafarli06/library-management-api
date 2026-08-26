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
		assertTrue(prodYaml.contains("port: ${PORT:8080}"));
		assertTrue(prodYaml.contains("${FRONTEND_ORIGIN:${CORS_ALLOWED_ORIGINS}}"));
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
		assertTrue(baseYaml.contains("http://localhost:5173"));
		assertTrue(baseYaml.contains("http://127.0.0.1:5173"));
		assertFalse(baseYaml.contains("FRONTEND_ORIGIN"));
		assertTrue(devYaml.contains("http://localhost:5173"));
		assertTrue(devYaml.contains("http://127.0.0.1:5173"));
		assertFalse(devYaml.contains("FRONTEND_ORIGIN"));
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

	@Test
	void corsOrigins_devKeepsLocalhostEvenWhenFrontendOriginIsSet() throws IOException {
		StandardEnvironment environment = new StandardEnvironment();
		environment.setActiveProfiles("dev");
		addYaml(environment, new YamlPropertySourceLoader(), "application.yml");
		addProfileYamlFirst(environment, new YamlPropertySourceLoader(), "application-dev.yml");
		environment.getPropertySources().addFirst(new MapPropertySource(
				"frontendOrigin",
				Map.of("FRONTEND_ORIGIN", "https://library-management-web-4tu2-woad.vercel.app")
		));

		String origins = environment.resolvePlaceholders(
				Objects.requireNonNull(environment.getProperty("app.cors.allowed-origins"))
		);

		assertTrue(origins.contains("http://localhost:5173"));
		assertTrue(origins.contains("http://127.0.0.1:5173"));
		assertFalse(origins.contains("vercel.app"));
	}

	@Test
	void corsOrigins_prodUsesFrontendOrigin() throws IOException {
		StandardEnvironment environment = new StandardEnvironment();
		environment.setActiveProfiles("prod");
		addYaml(environment, new YamlPropertySourceLoader(), "application.yml");
		addProfileYamlFirst(environment, new YamlPropertySourceLoader(), "application-prod.yml");
		environment.getPropertySources().addFirst(new MapPropertySource(
				"frontendOrigin",
				Map.of("FRONTEND_ORIGIN", "https://library-management-web-4tu2-woad.vercel.app")
		));

		String origins = environment.resolvePlaceholders(
				Objects.requireNonNull(environment.getProperty("app.cors.allowed-origins"))
		);

		assertEquals("https://library-management-web-4tu2-woad.vercel.app", origins);
	}

	private static void addProfileYamlFirst(
			StandardEnvironment environment,
			YamlPropertySourceLoader loader,
			String classpathLocation) throws IOException {
		ClassPathResource resource = new ClassPathResource(classpathLocation);
		loader.load(classpathLocation, resource).forEach(source ->
				environment.getPropertySources().addFirst(source)
		);
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
