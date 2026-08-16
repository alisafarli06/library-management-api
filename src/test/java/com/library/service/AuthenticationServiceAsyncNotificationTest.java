package com.library.service;

import com.library.dto.AuthenticationResponse;
import com.library.dto.RegisterRequest;
import com.library.dto.UserDto;
import com.library.entity.Role;
import com.library.exception.ConflictException;
import com.library.repository.UserRepository;
import com.library.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceAsyncNotificationTest {

	@Mock
	private UserService userService;

	@Mock
	private UserRepository userRepository;

	@Mock
	private JwtService jwtService;

	@Mock
	private AuthenticationManager authenticationManager;

	@Mock
	private UserDetailsService userDetailsService;

	@Mock
	private EmailNotificationService emailNotificationService;

	@Mock
	private PasswordEncoder passwordEncoder;

	@InjectMocks
	private AuthenticationService authenticationService;

	@Test
	void register_triggersWelcomeEmailWithoutWaitingOnNotificationService() {
		RegisterRequest request = new RegisterRequest();
		request.setFullName("Welcome User");
		request.setEmail("welcome@library.com");
		request.setPassword("Welcome123!");

		UserDto registered = new UserDto();
		registered.setId(1L);
		registered.setFullName("Welcome User");
		registered.setEmail("welcome@library.com");

		when(userService.register(any(UserDto.class))).thenReturn(registered);
		when(jwtService.generateAccessToken("welcome@library.com", Role.USER)).thenReturn("access-token");
		when(jwtService.generateRefreshToken("welcome@library.com", Role.USER)).thenReturn("refresh-token");

		AuthenticationResponse response = authenticationService.register(request);

		assertNotNull(response);
		assertEquals("access-token", response.getAccessToken());
		assertEquals("refresh-token", response.getRefreshToken());

		ArgumentCaptor<String> emailCaptor = ArgumentCaptor.forClass(String.class);
		verify(emailNotificationService).sendWelcomeEmail(emailCaptor.capture(), eq("Welcome User"));
		assertEquals("welcome@library.com", emailCaptor.getValue());
	}

	@Test
	void register_whenUserServiceFails_doesNotTriggerWelcomeEmail() {
		RegisterRequest request = new RegisterRequest();
		request.setFullName("Duplicate User");
		request.setEmail("duplicate@library.com");
		request.setPassword("Welcome123!");

		when(userService.register(any(UserDto.class)))
				.thenThrow(new ConflictException("Email already registered: duplicate@library.com"));

		assertThrows(ConflictException.class, () -> authenticationService.register(request));
		verify(emailNotificationService, never()).sendWelcomeEmail(any(), any());
	}
}
