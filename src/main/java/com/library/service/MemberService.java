package com.library.service;

import com.library.dto.MemberDto;
import com.library.entity.Member;
import com.library.exception.ResourceNotFoundException;
import com.library.repository.MemberRepository;
import org.springframework.stereotype.Service;

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
				.orElseThrow(() -> new ResourceNotFoundException("Member not found with id: " + id));
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
				.orElseThrow(() -> new ResourceNotFoundException("Member not found with id: " + id));
		member.setName(memberDto.getName());
		member.setEmail(memberDto.getEmail());
		Member saved = memberRepository.save(member);
		return toDto(saved);
	}

	public void delete(Long id) {
		if (!memberRepository.existsById(id)) {
			throw new ResourceNotFoundException("Member not found with id: " + id);
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
