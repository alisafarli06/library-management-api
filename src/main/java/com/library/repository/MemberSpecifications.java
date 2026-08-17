package com.library.repository;

import com.library.entity.Member;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public final class MemberSpecifications {

	private MemberSpecifications() {
	}

	/**
	 * Case-insensitive match against member name or email.
	 */
	public static Specification<Member> matchesQuery(String q) {
		return (root, query, builder) -> {
			if (query != null && query.getResultType() != Long.class && query.getResultType() != long.class) {
				root.fetch("user", JoinType.LEFT);
				query.distinct(true);
			}
			if (!StringUtils.hasText(q)) {
				return null;
			}
			String pattern = "%" + q.trim().toLowerCase() + "%";
			return builder.or(
					builder.like(builder.lower(root.get("name")), pattern),
					builder.like(builder.lower(root.get("email")), pattern)
			);
		};
	}
}
