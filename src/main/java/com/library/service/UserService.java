package com.library.service;

import com.library.dto.UserDto;
import com.library.entity.User;
import com.library.exception.ConflictException;
import com.library.mapper.UserMapper;
import com.library.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class UserService {

	private final UserRepository userRepository;
	private final UserMapper userMapper;
	private final PasswordEncoder passwordEncoder;

	public UserService(UserRepository userRepository, UserMapper userMapper, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.userMapper = userMapper;
		this.passwordEncoder = passwordEncoder;
	}

	@Transactional
	public UserDto register(UserDto userDto) {
		if (userRepository.findByEmail(userDto.getEmail()).isPresent()) {
			throw new ConflictException("Email already registered: " + userDto.getEmail());
		}

		User user = userMapper.toEntity(userDto);
		user.setId(null);
		user.setPassword(passwordEncoder.encode(userDto.getPassword()));
		User saved = userRepository.save(user);
		return userMapper.toDto(saved);
	}
}
