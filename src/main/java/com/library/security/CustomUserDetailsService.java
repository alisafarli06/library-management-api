package com.library.security;

import com.library.repository.UserRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

	private final UserRepository userRepository;

	public CustomUserDetailsService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		com.library.entity.User user = userRepository.findByEmail(username)
				.orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));

		return User.builder()
				.username(user.getEmail())
				.password(user.getPassword())
				.roles(user.getRole().name())
				.build();
	}
}
