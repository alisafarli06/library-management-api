package com.library.service;

import com.library.repository.FileMetadataRepository;
import com.library.service.storage.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class FileCleanupService {

	private static final Logger log = LoggerFactory.getLogger(FileCleanupService.class);

	private final FileStorageService fileStorageService;
	private final FileMetadataRepository fileMetadataRepository;

	public FileCleanupService(
			FileStorageService fileStorageService,
			FileMetadataRepository fileMetadataRepository) {
		this.fileStorageService = fileStorageService;
		this.fileMetadataRepository = fileMetadataRepository;
	}

	/**
	 * Deletes regular files in the upload directory that have no matching FileMetadata row.
	 */
	public CleanupResult cleanupOrphanedFiles() {
		log.info("File cleanup started");

		List<String> filenames = fileStorageService.listRegularFilenames();
		Set<String> referenced = new HashSet<>(fileMetadataRepository.findAllStoredFilenames());

		int scanned = filenames.size();
		int orphaned = 0;
		int deleted = 0;
		int failures = 0;

		for (String filename : filenames) {
			if (referenced.contains(filename)) {
				continue;
			}

			orphaned++;
			try {
				fileStorageService.delete(filename);
				deleted++;
				log.info("Deleted orphaned file: {}", filename);
			} catch (RuntimeException ex) {
				failures++;
				log.error("Failed to delete orphaned file: {}", filename, ex);
			}
		}

		log.info(
				"File cleanup completed: scanned={}, orphaned={}, deleted={}, failures={}",
				scanned,
				orphaned,
				deleted,
				failures
		);

		return new CleanupResult(scanned, orphaned, deleted, failures);
	}

	public record CleanupResult(int scanned, int orphaned, int deleted, int failures) {
	}
}
