package com.library.service;

import com.library.dto.MemberDto;
import com.library.entity.Member;
import com.library.exception.ResourceNotFoundException;
import com.library.mapper.MemberMapper;
import com.library.repository.MemberRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class MemberService {

	private final MemberRepository memberRepository;
	private final MemberMapper memberMapper;

	public MemberService(MemberRepository memberRepository, MemberMapper memberMapper) {
		this.memberRepository = memberRepository;
		this.memberMapper = memberMapper;
	}

	public Page<MemberDto> findAll(Pageable pageable) {
		return memberRepository.findAll(pageable).map(memberMapper::toDto);
	}

	public MemberDto findById(Long id) {
		Member member = memberRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Member not found with id: " + id));
		return memberMapper.toDto(member);
	}

	public MemberDto create(MemberDto memberDto) {
		Member member = memberMapper.toEntity(memberDto);
		member.setId(null);
		Member saved = memberRepository.save(member);
		return memberMapper.toDto(saved);
	}

	public MemberDto update(Long id, MemberDto memberDto) {
		Member member = memberRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Member not found with id: " + id));
		member.setName(memberDto.getName());
		member.setEmail(memberDto.getEmail());
		Member saved = memberRepository.save(member);
		return memberMapper.toDto(saved);
	}

	public void delete(Long id) {
		if (!memberRepository.existsById(id)) {
			throw new ResourceNotFoundException("Member not found with id: " + id);
		}
		memberRepository.deleteById(id);
	}
}
