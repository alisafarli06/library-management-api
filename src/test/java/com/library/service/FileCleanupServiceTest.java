package com.library.service;

import com.library.exception.FileStorageException;
import com.library.repository.FileMetadataRepository;
import com.library.service.storage.FileStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FileCleanupServiceTest {

	@Mock
	private FileStorageService fileStorageService;

	@Mock
	private FileMetadataRepository fileMetadataRepository;

	private FileCleanupService fileCleanupService;

	@BeforeEach
	void setUp() {
		fileCleanupService = new FileCleanupService(fileStorageService, fileMetadataRepository);
	}

	@Test
	void cleanup_deletesOrphanedFile() {
		when(fileStorageService.listRegularFilenames()).thenReturn(List.of("orphan.pdf"));
		when(fileMetadataRepository.findAllStoredFilenames()).thenReturn(List.of());

		FileCleanupService.CleanupResult result = fileCleanupService.cleanupOrphanedFiles();

		verify(fileStorageService).delete("orphan.pdf");
		assertEquals(1, result.scanned());
		assertEquals(1, result.orphaned());
		assertEquals(1, result.deleted());
		assertEquals(0, result.failures());
	}

	@Test
	void cleanup_preservesReferencedFile() {
		when(fileStorageService.listRegularFilenames()).thenReturn(List.of("kept.pdf"));
		when(fileMetadataRepository.findAllStoredFilenames()).thenReturn(List.of("kept.pdf"));

		FileCleanupService.CleanupResult result = fileCleanupService.cleanupOrphanedFiles();

		verify(fileStorageService, never()).delete("kept.pdf");
		assertEquals(1, result.scanned());
		assertEquals(0, result.orphaned());
		assertEquals(0, result.deleted());
	}

	@Test
	void cleanup_handlesMultipleOrphans() {
		when(fileStorageService.listRegularFilenames()).thenReturn(List.of("a.pdf", "b.pdf", "kept.pdf"));
		when(fileMetadataRepository.findAllStoredFilenames()).thenReturn(List.of("kept.pdf"));

		FileCleanupService.CleanupResult result = fileCleanupService.cleanupOrphanedFiles();

		verify(fileStorageService).delete("a.pdf");
		verify(fileStorageService).delete("b.pdf");
		verify(fileStorageService, never()).delete("kept.pdf");
		assertEquals(3, result.scanned());
		assertEquals(2, result.orphaned());
		assertEquals(2, result.deleted());
		assertEquals(0, result.failures());
	}

	@Test
	void cleanup_continuesWhenOneDeletionFails() {
		when(fileStorageService.listRegularFilenames()).thenReturn(List.of("bad.pdf", "good.pdf"));
		when(fileMetadataRepository.findAllStoredFilenames()).thenReturn(List.of());
		doThrow(new FileStorageException("locked")).when(fileStorageService).delete("bad.pdf");

		FileCleanupService.CleanupResult result = fileCleanupService.cleanupOrphanedFiles();

		InOrder order = inOrder(fileStorageService);
		order.verify(fileStorageService).delete("bad.pdf");
		order.verify(fileStorageService).delete("good.pdf");
		assertEquals(2, result.orphaned());
		assertEquals(1, result.deleted());
		assertEquals(1, result.failures());
	}

	@Test
	void cleanup_emptyDirectoryReturnsZeros() {
		when(fileStorageService.listRegularFilenames()).thenReturn(List.of());
		when(fileMetadataRepository.findAllStoredFilenames()).thenReturn(List.of());

		FileCleanupService.CleanupResult result = fileCleanupService.cleanupOrphanedFiles();

		verify(fileStorageService, never()).delete(org.mockito.ArgumentMatchers.anyString());
		assertEquals(0, result.scanned());
		assertEquals(0, result.orphaned());
		assertEquals(0, result.deleted());
		assertEquals(0, result.failures());
	}
}
