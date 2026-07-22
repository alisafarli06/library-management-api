package com.library.controller;

import com.library.dto.MemberDto;
import com.library.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

@RestController
@RequestMapping("/api/members")
@Tag(name = "Members", description = "CRUD operations for members")
public class MemberController {

	private final MemberService memberService;

	public MemberController(MemberService memberService) {
		this.memberService = memberService;
	}

	@GetMapping
	@Operation(summary = "List members", description = "Returns a paginated and sortable list of members")
	public Page<MemberDto> getAll(Pageable pageable) {
		return memberService.findAll(pageable);
	}

	@GetMapping("/{id}")
	@Operation(summary = "Get member by ID", description = "Returns a single member by its identifier")
	public MemberDto getById(@PathVariable Long id) {
		return memberService.findById(id);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Create member", description = "Creates a new library member")
	public MemberDto create(@Valid @RequestBody MemberDto memberDto) {
		return memberService.create(memberDto);
	}

	@PutMapping("/{id}")
	@Operation(summary = "Update member", description = "Updates an existing member by its identifier")
	public MemberDto update(@PathVariable Long id, @Valid @RequestBody MemberDto memberDto) {
		return memberService.update(id, memberDto);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Delete member", description = "Deletes a member by its identifier")
	public void delete(@PathVariable Long id) {
		memberService.delete(id);
	}
}
