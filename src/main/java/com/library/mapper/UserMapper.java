package com.library.mapper;

import com.library.dto.UserDto;
import com.library.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

	public UserDto toDto(User user) {
		UserDto dto = new UserDto();
		dto.setId(user.getId());
		dto.setFullName(user.getFullName());
		dto.setEmail(user.getEmail());
		dto.setCreatedAt(user.getCreatedAt());
		return dto;
	}

	public User toEntity(UserDto dto) {
		User user = new User();
		user.setId(dto.getId());
		user.setFullName(dto.getFullName());
		user.setEmail(dto.getEmail());
		user.setPassword(dto.getPassword());
		return user;
	}
}
