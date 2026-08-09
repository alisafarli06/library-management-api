package com.library.service.storage;

import com.library.config.FileStorageProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LocalFileStorageListAndCleanupSafetyTest {

	@TempDir
	Path tempDir;

	private LocalFileStorageService storageService;

	@BeforeEach
	void setUp() {
		FileStorageProperties properties = new FileStorageProperties();
		properties.setStorageDirectory(tempDir.toString());
		storageService = new LocalFileStorageService(properties);
		storageService.init();
	}

	@Test
	void listRegularFilenames_ignoresDirectoriesAndOnlyReturnsFiles() throws Exception {
		Files.writeString(tempDir.resolve("file-a.pdf"), "a");
		Files.writeString(tempDir.resolve("file-b.png"), "b");
		Files.createDirectory(tempDir.resolve("subdir"));
		Files.writeString(tempDir.resolve("subdir").resolve("nested.pdf"), "nested");

		List<String> names = storageService.listRegularFilenames();

		assertEquals(2, names.size());
		assertTrue(names.contains("file-a.pdf"));
		assertTrue(names.contains("file-b.png"));
		assertFalse(names.contains("subdir"));
		assertFalse(names.contains("nested.pdf"));
	}

	@Test
	void listRegularFilenames_emptyDirectoryReturnsEmptyList() {
		assertTrue(storageService.listRegularFilenames().isEmpty());
	}

	@Test
	void delete_onlyAffectsFilesInsideConfiguredRoot() throws Exception {
		Path outside = Files.createTempFile("outside-cleanup", ".pdf");
		try {
			Files.writeString(outside, "keep-me");
			Files.writeString(tempDir.resolve("orphan.pdf"), "delete-me");

			storageService.delete("orphan.pdf");

			assertFalse(Files.exists(tempDir.resolve("orphan.pdf")));
			assertTrue(Files.exists(outside));
		} finally {
			Files.deleteIfExists(outside);
		}
	}
}
