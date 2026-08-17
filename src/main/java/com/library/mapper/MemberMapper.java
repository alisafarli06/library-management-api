package com.library.mapper;

import com.library.dto.MemberDto;
import com.library.entity.Member;
import com.library.entity.User;
import org.springframework.stereotype.Component;

@Component
public class MemberMapper {

	public MemberDto toDto(Member member) {
		MemberDto dto = new MemberDto();
		dto.setId(member.getId());
		dto.setName(member.getName());
		dto.setEmail(member.getEmail());
		dto.setActiveLoanCount(member.getActiveLoanCount() != null ? member.getActiveLoanCount() : 0L);
		User user = member.getUser();
		if (user != null) {
			dto.setUserId(user.getId());
			dto.setRole(user.getRole());
			dto.setStatus(user.getStatus());
		}
		return dto;
	}

	public Member toEntity(MemberDto dto) {
		Member member = new Member();
		member.setId(dto.getId());
		member.setName(dto.getName());
		member.setEmail(dto.getEmail());
		return member;
	}
}
