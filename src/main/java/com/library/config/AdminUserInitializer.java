package com.library.config;

import com.library.entity.Role;
import com.library.entity.User;
import com.library.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class AdminUserInitializer implements CommandLineRunner {

	private static final Logger log = LoggerFactory.getLogger(AdminUserInitializer.class);

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final AdminUserProperties adminUserProperties;

	public AdminUserInitializer(
			UserRepository userRepository,
			PasswordEncoder passwordEncoder,
			AdminUserProperties adminUserProperties) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.adminUserProperties = adminUserProperties;
	}

	@Override
	public void run(String... args) {
		String adminEmail = adminUserProperties.getEmail();
		if (!StringUtils.hasText(adminEmail) || !StringUtils.hasText(adminUserProperties.getPassword())) {
			log.warn("Skipping bootstrap admin user: app.admin.email and app.admin.password must be set");
			return;
		}

		if (userRepository.findByEmail(adminEmail).isEmpty()) {
			User admin = new User();
			admin.setFullName(adminUserProperties.getFullName());
			admin.setEmail(adminEmail);
			admin.setPassword(passwordEncoder.encode(adminUserProperties.getPassword()));
			admin.setRole(Role.ADMIN);
			userRepository.save(admin);
			log.info("Created bootstrap ADMIN user {}", adminEmail);
		}
	}
}
