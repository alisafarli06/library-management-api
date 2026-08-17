package com.library.repository;

import com.library.entity.Member;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long>, JpaSpecificationExecutor<Member> {

	@Override
	@EntityGraph(attributePaths = "user")
	Optional<Member> findById(Long id);

	boolean existsByIdAndBooks_Id(Long memberId, Long bookId);

	Optional<Member> findByUser_Id(Long userId);

	Optional<Member> findByEmail(String email);
}
