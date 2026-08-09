package com.library.scheduler;

import com.library.service.FileCleanupService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class FileCleanupSchedulerTest {

	@Mock
	private FileCleanupService fileCleanupService;

	@InjectMocks
	private FileCleanupScheduler fileCleanupScheduler;

	@Test
	void runCleanup_invokesCleanupService() {
		fileCleanupScheduler.runCleanup();

		verify(fileCleanupService).cleanupOrphanedFiles();
	}
}
