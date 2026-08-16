package com.library.repository;

import com.library.entity.Loan;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public final class LoanSpecifications {

	private LoanSpecifications() {
	}

	/**
	 * Case-insensitive match against member name, member email, or book title.
	 */
	public static Specification<Loan> matchesQuery(String q) {
		return (root, query, builder) -> {
			if (!StringUtils.hasText(q)) {
				return null;
			}
			String pattern = "%" + q.trim().toLowerCase() + "%";
			var member = root.join("member", JoinType.INNER);
			var book = root.join("book", JoinType.INNER);
			if (query != null && Long.class != query.getResultType() && long.class != query.getResultType()) {
				query.distinct(true);
			}
			return builder.or(
					builder.like(builder.lower(member.get("name")), pattern),
					builder.like(builder.lower(member.get("email")), pattern),
					builder.like(builder.lower(book.get("title")), pattern)
			);
		};
	}

	/**
	 * @param status {@code borrowed} (active), {@code returned}, or {@code all}/blank (no filter)
	 */
	public static Specification<Loan> status(String status) {
		return (root, query, builder) -> {
			if (!StringUtils.hasText(status)) {
				return null;
			}
			String normalized = status.trim().toLowerCase();
			if ("all".equals(normalized)) {
				return null;
			}
			if ("borrowed".equals(normalized)) {
				return builder.isNull(root.get("returnedAt"));
			}
			if ("returned".equals(normalized)) {
				return builder.isNotNull(root.get("returnedAt"));
			}
			return null;
		};
	}
}
