package com.library.service;

import com.library.config.FileStorageProperties;
import com.library.repository.FileMetadataRepository;
import com.library.service.storage.LocalFileStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FileCleanupServiceFilesystemTest {

	@TempDir
	Path tempDir;

	@Mock
	private FileMetadataRepository fileMetadataRepository;

	private LocalFileStorageService storageService;
	private FileCleanupService fileCleanupService;

	@BeforeEach
	void setUp() {
		FileStorageProperties properties = new FileStorageProperties();
		properties.setStorageDirectory(tempDir.toString());
		storageService = new LocalFileStorageService(properties);
		fileCleanupService = new FileCleanupService(storageService, fileMetadataRepository);
	}

	@Test
	void cleanup_deletesOrphansAndPreservesReferencedFilesOnDisk() throws Exception {
		Files.writeString(tempDir.resolve("orphan-1.pdf"), "x");
		Files.writeString(tempDir.resolve("orphan-2.pdf"), "y");
		Files.writeString(tempDir.resolve("kept.pdf"), "z");
		Files.createDirectory(tempDir.resolve("subdir"));

		when(fileMetadataRepository.findAllStoredFilenames()).thenReturn(List.of("kept.pdf"));

		FileCleanupService.CleanupResult result = fileCleanupService.cleanupOrphanedFiles();

		assertFalse(Files.exists(tempDir.resolve("orphan-1.pdf")));
		assertFalse(Files.exists(tempDir.resolve("orphan-2.pdf")));
		assertTrue(Files.exists(tempDir.resolve("kept.pdf")));
		assertTrue(Files.isDirectory(tempDir.resolve("subdir")));
		assertEquals(3, result.scanned());
		assertEquals(2, result.orphaned());
		assertEquals(2, result.deleted());
		assertEquals(0, result.failures());
	}
}
