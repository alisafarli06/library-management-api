package com.library.service;

import com.library.dto.AuthenticationResponse;
import com.library.dto.LoginRequest;
import com.library.dto.RefreshTokenRequest;
import com.library.dto.RegisterRequest;
import com.library.dto.UserDto;
import com.library.entity.Role;
import com.library.security.JwtService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AuthenticationService {

	private final UserService userService;
	private final JwtService jwtService;
	private final AuthenticationManager authenticationManager;
	private final UserDetailsService userDetailsService;
	private final EmailNotificationService emailNotificationService;

	public AuthenticationService(
			UserService userService,
			JwtService jwtService,
			AuthenticationManager authenticationManager,
			UserDetailsService userDetailsService,
			EmailNotificationService emailNotificationService) {
		this.userService = userService;
		this.jwtService = jwtService;
		this.authenticationManager = authenticationManager;
		this.userDetailsService = userDetailsService;
		this.emailNotificationService = emailNotificationService;
	}

	@Transactional
	public AuthenticationResponse register(RegisterRequest request) {
		UserDto userDto = new UserDto();
		userDto.setFullName(request.getFullName());
		userDto.setEmail(request.getEmail());
		userDto.setPassword(request.getPassword());

		UserDto registeredUser = userService.register(userDto);
		emailNotificationService.sendWelcomeEmail(registeredUser.getEmail(), registeredUser.getFullName());
		return issueTokens(registeredUser.getEmail(), Role.USER);
	}

	public AuthenticationResponse login(LoginRequest request) {
		Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
		);
		return issueTokens(request.getEmail(), extractRole(authentication));
	}

	public AuthenticationResponse refresh(RefreshTokenRequest request) {
		try {
			String refreshToken = request.getRefreshToken();
			String email = jwtService.extractEmail(refreshToken);
			String roleClaim = jwtService.extractRole(refreshToken);

			if (!jwtService.isRefreshToken(refreshToken) || roleClaim == null) {
				throw new InsufficientAuthenticationException("Invalid refresh token");
			}

			userDetailsService.loadUserByUsername(email);
			return issueTokens(email, Role.valueOf(roleClaim));
		} catch (ExpiredJwtException ex) {
			throw new CredentialsExpiredException("Refresh token expired");
		} catch (UsernameNotFoundException | JwtException | IllegalArgumentException ex) {
			throw new InsufficientAuthenticationException("Invalid refresh token");
		}
	}

	private AuthenticationResponse issueTokens(String email, Role role) {
		return new AuthenticationResponse(
				jwtService.generateAccessToken(email, role),
				jwtService.generateRefreshToken(email, role)
		);
	}

	private Role extractRole(Authentication authentication) {
		return authentication.getAuthorities().stream()
				.map(GrantedAuthority::getAuthority)
				.filter(authority -> authority.startsWith("ROLE_"))
				.map(authority -> authority.substring(5))
				.map(Role::valueOf)
				.findFirst()
				.orElseThrow(() -> new IllegalStateException("Authenticated user has no role"));
	}
}
