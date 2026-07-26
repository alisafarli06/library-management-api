package com.library.service;

import com.library.dto.AuthenticationResponse;
import com.library.dto.LoginRequest;
import com.library.dto.RegisterRequest;
import com.library.dto.UserDto;
import com.library.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AuthenticationService {

	private final UserService userService;
	private final JwtService jwtService;
	private final AuthenticationManager authenticationManager;

	public AuthenticationService(
			UserService userService,
			JwtService jwtService,
			AuthenticationManager authenticationManager) {
		this.userService = userService;
		this.jwtService = jwtService;
		this.authenticationManager = authenticationManager;
	}

	@Transactional
	public AuthenticationResponse register(RegisterRequest request) {
		UserDto userDto = new UserDto();
		userDto.setFullName(request.getFullName());
		userDto.setEmail(request.getEmail());
		userDto.setPassword(request.getPassword());

		UserDto registeredUser = userService.register(userDto);
		String token = jwtService.generateToken(registeredUser.getEmail());
		return new AuthenticationResponse(token);
	}

	public AuthenticationResponse login(LoginRequest request) {
		authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
		);
		String token = jwtService.generateToken(request.getEmail());
		return new AuthenticationResponse(token);
	}
}
