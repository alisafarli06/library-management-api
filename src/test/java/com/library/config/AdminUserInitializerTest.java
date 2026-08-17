package com.library.config;

import com.library.entity.Role;
import com.library.entity.User;
import com.library.repository.UserRepository;
import com.library.service.MemberService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminUserInitializerTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private MemberService memberService;

	private final AdminUserProperties properties = new AdminUserProperties();
	private AdminUserInitializer initializer;

	@BeforeEach
	void setUp() {
		properties.setEmail("alisafarli@gmail.com");
		properties.setFullName("Ali Safarli");
		properties.setPassword("from-env-only");
		initializer = new AdminUserInitializer(userRepository, passwordEncoder, properties, memberService);
	}

	@Test
	void createsAdminOnceWhenMissing() {
		when(userRepository.findByEmail("alisafarli@gmail.com")).thenReturn(Optional.empty());
		when(passwordEncoder.encode("from-env-only")).thenReturn("bcrypt-hash");
		when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
			User user = invocation.getArgument(0);
			user.setId(1L);
			return user;
		});

		initializer.run();

		ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
		verify(userRepository).save(captor.capture());
		User saved = captor.getValue();
		assertEquals("Ali Safarli", saved.getFullName());
		assertEquals("alisafarli@gmail.com", saved.getEmail());
		assertEquals("bcrypt-hash", saved.getPassword());
		assertEquals(Role.ADMIN, saved.getRole());
		verify(passwordEncoder).encode("from-env-only");
		verify(memberService).ensureMemberForUser(saved);
	}

	@Test
	void doesNotCreateDuplicateOrOverwritePasswordWhenAdminExists() {
		User existing = existingAdmin();
		when(userRepository.findByEmail("alisafarli@gmail.com")).thenReturn(Optional.of(existing));

		initializer.run();
		initializer.run();

		verify(userRepository, never()).save(any(User.class));
		verify(passwordEncoder, never()).encode(any());
		verify(memberService, times(2)).ensureMemberForUser(existing);
	}

	@Test
	void promotesExistingUserWithBootstrapEmailToAdminWithoutChangingPassword() {
		User existing = new User();
		existing.setId(7L);
		existing.setFullName("Ali Safarli");
		existing.setEmail("alisafarli@gmail.com");
		existing.setPassword("already-hashed");
		existing.setRole(Role.USER);
		when(userRepository.findByEmail("alisafarli@gmail.com")).thenReturn(Optional.of(existing));
		when(userRepository.save(existing)).thenReturn(existing);

		initializer.run();

		assertEquals(Role.ADMIN, existing.getRole());
		assertEquals("already-hashed", existing.getPassword());
		verify(passwordEncoder, never()).encode(any());
		verify(userRepository).save(existing);
	}

	private static User existingAdmin() {
		User existing = new User();
		existing.setId(1L);
		existing.setFullName("Ali Safarli");
		existing.setEmail("alisafarli@gmail.com");
		existing.setPassword("already-hashed");
		existing.setRole(Role.ADMIN);
		return existing;
	}
}
