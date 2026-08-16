package com.library.service;

import com.library.dto.UserDto;
import com.library.dto.UserProfileDto;
import com.library.entity.Role;
import com.library.entity.User;
import com.library.exception.ConflictException;
import com.library.exception.ResourceNotFoundException;
import com.library.mapper.UserMapper;
import com.library.repository.MemberRepository;
import com.library.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class UserService {

	private final UserRepository userRepository;
	private final MemberRepository memberRepository;
	private final UserMapper userMapper;
	private final PasswordEncoder passwordEncoder;
	private final MemberService memberService;

	public UserService(
			UserRepository userRepository,
			MemberRepository memberRepository,
			UserMapper userMapper,
			PasswordEncoder passwordEncoder,
			MemberService memberService) {
		this.userRepository = userRepository;
		this.memberRepository = memberRepository;
		this.userMapper = userMapper;
		this.passwordEncoder = passwordEncoder;
		this.memberService = memberService;
	}

	public UserProfileDto getProfile(String email) {
		User user = requireUserByEmail(email);
		return toProfileDto(user);
	}

	@Transactional
	public UserProfileDto updateProfile(String email, String name) {
		User user = requireUserByEmail(email);
		String trimmed = name.trim();
		user.setFullName(trimmed);
		User saved = userRepository.save(user);

		memberRepository.findByUser_Id(saved.getId()).ifPresent(member -> {
			member.setName(trimmed);
			memberRepository.save(member);
		});

		return toProfileDto(saved);
	}

	@Transactional
	public UserDto register(UserDto userDto) {
		if (userRepository.findByEmail(userDto.getEmail()).isPresent()) {
			throw new ConflictException("Email already registered: " + userDto.getEmail());
		}

		User user = userMapper.toEntity(userDto);
		user.setId(null);
		user.setRole(Role.USER);
		user.setPassword(passwordEncoder.encode(userDto.getPassword()));
		User saved = userRepository.save(user);
		memberService.ensureMemberForUser(saved);
		return userMapper.toDto(saved);
	}

	private User requireUserByEmail(String email) {
		return userRepository.findByEmail(email)
				.orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
	}

	private UserProfileDto toProfileDto(User user) {
		return new UserProfileDto(user.getFullName(), user.getEmail());
	}
}
