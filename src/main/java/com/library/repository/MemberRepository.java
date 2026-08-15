package com.library.repository;

import com.library.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

	boolean existsByIdAndBooks_Id(Long memberId, Long bookId);

	Optional<Member> findByUser_Id(Long userId);

	Optional<Member> findByEmail(String email);
}
