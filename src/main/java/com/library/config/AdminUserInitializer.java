package com.library.config;

import com.library.entity.AccountStatus;
import com.library.entity.Role;
import com.library.entity.User;
import com.library.repository.UserRepository;
import com.library.service.MemberService;
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
	private final MemberService memberService;

	public AdminUserInitializer(
			UserRepository userRepository,
			PasswordEncoder passwordEncoder,
			AdminUserProperties adminUserProperties,
			MemberService memberService) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.adminUserProperties = adminUserProperties;
		this.memberService = memberService;
	}

	@Override
	public void run(String... args) {
		String adminEmail = adminUserProperties.getEmail();
		if (!StringUtils.hasText(adminEmail) || !StringUtils.hasText(adminUserProperties.getPassword())) {
			log.warn("Skipping bootstrap admin user: app.admin.email and app.admin.password must be set");
			return;
		}

		User admin = userRepository.findByEmail(adminEmail).orElseGet(() -> createBootstrapAdmin(adminEmail));
		if (admin.getRole() != Role.ADMIN) {
			admin.setRole(Role.ADMIN);
			admin = userRepository.save(admin);
			log.info("Promoted existing user {} to ADMIN", adminEmail);
		}
		memberService.ensureMemberForUser(admin);
	}

	private User createBootstrapAdmin(String adminEmail) {
		User created = new User();
		created.setFullName(adminUserProperties.getFullName());
		created.setEmail(adminEmail);
		created.setPassword(passwordEncoder.encode(adminUserProperties.getPassword()));
		created.setRole(Role.ADMIN);
		created.setStatus(AccountStatus.ACTIVE);
		User saved = userRepository.save(created);
		log.info("Created bootstrap ADMIN user {}", adminEmail);
		return saved;
	}
}
