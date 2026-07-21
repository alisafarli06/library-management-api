package com.library.controller;

import com.library.dto.MemberDto;
import com.library.service.MemberService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/members")
public class MemberController {

	private final MemberService memberService;

	public MemberController(MemberService memberService) {
		this.memberService = memberService;
	}

	@GetMapping
	public List<MemberDto> getAll() {
		return memberService.findAll();
	}

	@GetMapping("/{id}")
	public MemberDto getById(@PathVariable Long id) {
		return memberService.findById(id);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public MemberDto create(@RequestBody MemberDto memberDto) {
		return memberService.create(memberDto);
	}

	@PutMapping("/{id}")
	public MemberDto update(@PathVariable Long id, @RequestBody MemberDto memberDto) {
		return memberService.update(id, memberDto);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable Long id) {
		memberService.delete(id);
	}
}
