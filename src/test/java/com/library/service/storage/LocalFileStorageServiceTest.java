package com.library.service.storage;

import com.library.config.FileStorageProperties;
import com.library.exception.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LocalFileStorageServiceTest {

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
	void store_writesFileUnderConfiguredDirectory() throws Exception {
		MockMultipartFile file = new MockMultipartFile(
				"file",
				"report.pdf",
				"application/pdf",
				"%PDF-1.4 test".getBytes()
		);

		storageService.store(file, "abc-123.pdf");

		Path stored = tempDir.resolve("abc-123.pdf");
		assertTrue(Files.exists(stored));
		assertEquals("%PDF-1.4 test", Files.readString(stored));
	}

	@Test
	void resolveSafePath_rejectsPathTraversal() {
		assertThrows(BadRequestException.class, () -> storageService.resolveSafePath("../secret.txt"));
		assertThrows(BadRequestException.class, () -> storageService.resolveSafePath("..\\secret.txt"));
		assertThrows(BadRequestException.class, () -> storageService.resolveSafePath("../../etc/passwd"));
	}

	@Test
	void resolveSafePath_rejectsAbsoluteAndNestedPaths() {
		assertThrows(BadRequestException.class, () -> storageService.resolveSafePath("/tmp/evil.pdf"));
		assertThrows(BadRequestException.class, () -> storageService.resolveSafePath("nested/evil.pdf"));
	}

	@Test
	void delete_removesStoredFile() throws Exception {
		MockMultipartFile file = new MockMultipartFile(
				"file",
				"a.png",
				"image/png",
				new byte[] {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A}
		);
		storageService.store(file, "to-delete.png");
		assertTrue(Files.exists(tempDir.resolve("to-delete.png")));

		storageService.delete("to-delete.png");

		assertFalse(Files.exists(tempDir.resolve("to-delete.png")));
	}
}
