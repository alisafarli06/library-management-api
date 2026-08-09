package com.library.scheduler;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest
@TestPropertySource(properties = {
		"app.file.cleanup.enabled=false",
		"app.file.cleanup.cron=0 15 4 * * *"
})
class FileCleanupSchedulerDisabledTest {

	@Autowired
	private ApplicationContext applicationContext;

	@Value("${app.file.cleanup.cron}")
	private String cleanupCron;

	@Value("${app.file.cleanup.enabled}")
	private boolean cleanupEnabled;

	@Test
	void cleanupDisabled_doesNotRegisterSchedulerBean() {
		assertFalse(cleanupEnabled);
		assertEquals(0, applicationContext.getBeanNamesForType(FileCleanupScheduler.class).length);
	}

	@Test
	void cronExpressionIsReadFromConfiguration() {
		assertEquals("0 15 4 * * *", cleanupCron);
	}
}
