package com.library.service;

import com.library.dto.AdminUserDto;
import com.library.entity.AccountStatus;
import com.library.entity.Member;
import com.library.entity.Role;
import com.library.entity.User;
import com.library.exception.BadRequestException;
import com.library.exception.ConflictException;
import com.library.exception.ResourceNotFoundException;
import com.library.repository.LoanRepository;
import com.library.repository.MemberRepository;
import com.library.repository.UserRepository;
import com.library.repository.UserSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AdminUserService {

	public static final String ACCOUNT_BLOCKED_MESSAGE = "Account is blocked";
	public static final String LAST_ADMIN_MESSAGE = "Cannot remove, block, or delete the last remaining ADMIN account";

	private final UserRepository userRepository;
	private final MemberRepository memberRepository;
	private final LoanRepository loanRepository;

	public AdminUserService(
			UserRepository userRepository,
			MemberRepository memberRepository,
			LoanRepository loanRepository) {
		this.userRepository = userRepository;
		this.memberRepository = memberRepository;
		this.loanRepository = loanRepository;
	}

	public Page<AdminUserDto> search(String q, Role role, AccountStatus status, Pageable pageable) {
		Specification<User> specification = Specification.allOf(
				UserSpecifications.matchesQuery(q),
				UserSpecifications.hasRole(role),
				UserSpecifications.hasStatus(status)
		);
		return userRepository.findAll(specification, pageable).map(this::toDto);
	}

	public AdminUserDto findById(Long id) {
		return toDto(requireUser(id));
	}

	@Transactional
	public AdminUserDto updateRole(Long id, Role role, String actorEmail) {
		User user = requireUser(id);
		if (isSelf(user, actorEmail)) {
			throw new BadRequestException("You cannot change your own role");
		}
		if (user.getRole() == role) {
			return toDto(user);
		}
		if (user.getRole() == Role.ADMIN && role == Role.USER) {
			ensureNotLastActiveAdmin(user);
		}
		user.setRole(role);
		return toDto(userRepository.save(user));
	}

	@Transactional
	public AdminUserDto updateStatus(Long id, AccountStatus status, String actorEmail) {
		User user = requireUser(id);
		if (isSelf(user, actorEmail)) {
			throw new BadRequestException("You cannot block or unblock your own account");
		}
		if (user.getStatus() == status) {
			return toDto(user);
		}
		if (user.getRole() == Role.ADMIN && status == AccountStatus.BLOCKED) {
			ensureNotLastActiveAdmin(user);
		}
		user.setStatus(status);
		return toDto(userRepository.save(user));
	}

	@Transactional
	public void delete(Long id, String actorEmail) {
		User user = requireUser(id);
		if (isSelf(user, actorEmail)) {
			throw new BadRequestException("You cannot delete your own account");
		}
		if (user.getRole() == Role.ADMIN) {
			ensureNotLastActiveAdmin(user);
		}

		memberRepository.findByUser_Id(user.getId()).ifPresent(this::deleteLinkedMember);
		userRepository.delete(user);
	}

	private void deleteLinkedMember(Member member) {
		if (loanRepository.existsByMember_Id(member.getId())) {
			throw new ConflictException("User cannot be deleted because the linked member has borrow records");
		}
		memberRepository.delete(member);
	}

	private void ensureNotLastActiveAdmin(User user) {
		if (user.getStatus() != AccountStatus.ACTIVE) {
			return;
		}
		if (userRepository.countByRoleAndStatus(Role.ADMIN, AccountStatus.ACTIVE) <= 1) {
			throw new ConflictException(LAST_ADMIN_MESSAGE);
		}
	}

	private boolean isSelf(User user, String actorEmail) {
		return actorEmail != null && user.getEmail().equalsIgnoreCase(actorEmail);
	}

	private User requireUser(Long id) {
		return userRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
	}

	private AdminUserDto toDto(User user) {
		return new AdminUserDto(
				user.getId(),
				user.getFullName(),
				user.getEmail(),
				user.getRole(),
				user.getStatus(),
				user.getCreatedAt()
		);
	}
}
