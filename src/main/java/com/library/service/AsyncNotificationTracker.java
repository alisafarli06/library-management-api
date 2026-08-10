package com.library.service;

import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory tracker for completed async notifications (used by tests and diagnostics).
 */
@Component
public class AsyncNotificationTracker {

	private final Set<String> welcomeEmailsSent = ConcurrentHashMap.newKeySet();

	public void markWelcomeEmailSent(String email) {
		if (email != null) {
			welcomeEmailsSent.add(email);
		}
	}

	public boolean wasWelcomeEmailSent(String email) {
		return welcomeEmailsSent.contains(email);
	}

	public void clear() {
		welcomeEmailsSent.clear();
	}
}
