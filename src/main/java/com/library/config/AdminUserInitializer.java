package com.library.config;

import com.library.entity.Role;
import com.library.entity.User;
import com.library.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminUserInitializer implements CommandLineRunner {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	public AdminUserInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public void run(String... args) {
		String adminEmail = "admin@library.com";

		if (userRepository.findByEmail(adminEmail).isEmpty()) {
			User admin = new User();
			admin.setFullName("Library Admin");
			admin.setEmail(adminEmail);
			admin.setPassword(passwordEncoder.encode("Admin123!"));
			admin.setRole(Role.ADMIN);
			userRepository.save(admin);
		}
	}
}
