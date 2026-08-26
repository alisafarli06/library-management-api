package com.library.service;

import com.library.config.AsyncConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

import static java.time.Duration.ofMillis;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Loads only async-related beans so @Async proxy behavior is verified without a database.
 */
@SpringBootTest(classes = {
		AsyncConfig.class,
		EmailNotificationService.class,
		AsyncNotificationTracker.class
})
@TestPropertySource(properties = {
		"app.async.notification.delay-ms=1000",
		"app.async.notification.simulate-failure=false",
		"app.async.executor.core-pool-size=2",
		"app.async.executor.max-pool-size=4",
		"app.async.executor.queue-capacity=100"
})
class EmailNotificationServiceAsyncTest {

	@Autowired
	private EmailNotificationService emailNotificationService;

	@Autowired
	private AsyncNotificationTracker notificationTracker;

	@BeforeEach
	void setUp() {
		notificationTracker.clear();
	}

	@Test
	void emailNotificationService_isSpringAopProxy() {
		assertTrue(AopUtils.isAopProxy(emailNotificationService));
	}

	@Test
	void sendWelcomeEmail_doesNotBlockCaller() {
		String email = "async-fast-" + UUID.randomUUID() + "@library.com";

		assertTimeoutPreemptively(ofMillis(400), () ->
				emailNotificationService.sendWelcomeEmail(email, "Async User")
		);

		assertFalse(
				notificationTracker.wasWelcomeEmailSent(email),
				"Notification should not have finished yet while caller already returned"
		);
	}

	@Test
	void sendWelcomeEmail_eventuallyCompletes() throws Exception {
		String email = "async-done-" + UUID.randomUUID() + "@library.com";

		emailNotificationService.sendWelcomeEmail(email, "Async User");

		boolean completed = waitUntil(() -> notificationTracker.wasWelcomeEmailSent(email), 3_000);
		assertTrue(completed, "Async welcome email should complete after the simulated delay");
	}

	@Test
	void sendWelcomeEmail_failureDoesNotMarkAsSent() throws Exception {
		EmailNotificationService failingService = new EmailNotificationService(50, true, notificationTracker);
		String email = "async-fail-" + UUID.randomUUID() + "@library.com";

		failingService.sendWelcomeEmail(email, "Failing User");

		TimeUnit.MILLISECONDS.sleep(200);
		assertFalse(notificationTracker.wasWelcomeEmailSent(email));
	}

	private static boolean waitUntil(BooleanSupplier condition, long timeoutMs) throws InterruptedException {
		long deadline = System.currentTimeMillis() + timeoutMs;
		while (System.currentTimeMillis() < deadline) {
			if (condition.getAsBoolean()) {
				return true;
			}
			TimeUnit.MILLISECONDS.sleep(50);
		}
		return condition.getAsBoolean();
	}
}
