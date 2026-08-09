package com.library.service.storage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FileStorageService {

	/**
	 * Stores the file under the given generated storage filename.
	 * The storage filename must not contain path separators.
	 */
	void store(MultipartFile file, String storedFilename);

	/**
	 * Loads a previously stored file as a readable resource.
	 */
	Resource loadAsResource(String storedFilename);

	/**
	 * Deletes a stored file if it exists. Missing files are ignored.
	 */
	void delete(String storedFilename);

	/**
	 * Lists names of regular files directly under the storage root (non-recursive).
	 */
	List<String> listRegularFilenames();
}
