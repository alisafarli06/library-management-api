package com.library.service;

import com.library.dto.MemberDto;
import com.library.entity.Member;
import com.library.repository.MemberRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class MemberService {

	private final MemberRepository memberRepository;

	public MemberService(MemberRepository memberRepository) {
		this.memberRepository = memberRepository;
	}

	public List<MemberDto> findAll() {
		return memberRepository.findAll().stream()
				.map(this::toDto)
				.toList();
	}

	public MemberDto findById(Long id) {
		Member member = memberRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Member not found"));
		return toDto(member);
	}

	public MemberDto create(MemberDto memberDto) {
		Member member = toEntity(memberDto);
		member.setId(null);
		Member saved = memberRepository.save(member);
		return toDto(saved);
	}

	public MemberDto update(Long id, MemberDto memberDto) {
		Member member = memberRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Member not found"));
		member.setName(memberDto.getName());
		member.setEmail(memberDto.getEmail());
		Member saved = memberRepository.save(member);
		return toDto(saved);
	}

	public void delete(Long id) {
		if (!memberRepository.existsById(id)) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Member not found");
		}
		memberRepository.deleteById(id);
	}

	private MemberDto toDto(Member member) {
		MemberDto dto = new MemberDto();
		dto.setId(member.getId());
		dto.setName(member.getName());
		dto.setEmail(member.getEmail());
		return dto;
	}

	private Member toEntity(MemberDto dto) {
		Member member = new Member();
		member.setId(dto.getId());
		member.setName(dto.getName());
		member.setEmail(dto.getEmail());
		return member;
	}
}
