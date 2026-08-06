package com.library.security;

import com.library.entity.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

	private static final String CLAIM_TOKEN_TYPE = "type";
	private static final String CLAIM_ROLE = "role";
	private static final String ACCESS_TOKEN_TYPE = "access";
	private static final String REFRESH_TOKEN_TYPE = "refresh";

	private final SecretKey secretKey;
	private final long accessExpirationMs;
	private final long refreshExpirationMs;

	public JwtService(
			@Value("${app.jwt.secret}") String secret,
			@Value("${app.jwt.access-expiration-ms}") long accessExpirationMs,
			@Value("${app.jwt.refresh-expiration-ms}") long refreshExpirationMs) {
		this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
		this.accessExpirationMs = accessExpirationMs;
		this.refreshExpirationMs = refreshExpirationMs;
	}

	public String generateAccessToken(String email, Role role) {
		return generateToken(email, role, accessExpirationMs, ACCESS_TOKEN_TYPE);
	}

	public String generateRefreshToken(String email, Role role) {
		return generateToken(email, role, refreshExpirationMs, REFRESH_TOKEN_TYPE);
	}

	public String generateToken(String email, Role role) {
		return generateAccessToken(email, role);
	}

	public String generateToken(String email, Role role, long customExpirationMs) {
		return generateToken(email, role, customExpirationMs, ACCESS_TOKEN_TYPE);
	}

	public String generateRefreshToken(String email, Role role, long customExpirationMs) {
		return generateToken(email, role, customExpirationMs, REFRESH_TOKEN_TYPE);
	}

	public String extractEmail(String token) {
		return parseClaims(token).getSubject();
	}

	public String extractRole(String token) {
		return parseClaims(token).get(CLAIM_ROLE, String.class);
	}

	public boolean isAccessToken(String token) {
		return ACCESS_TOKEN_TYPE.equals(extractTokenType(token));
	}

	public boolean isRefreshToken(String token) {
		return REFRESH_TOKEN_TYPE.equals(extractTokenType(token));
	}

	public boolean isTokenValid(String token, String email) {
		String subject = extractEmail(token);
		return subject.equals(email) && !isTokenExpired(token);
	}

	private String extractTokenType(String token) {
		return parseClaims(token).get(CLAIM_TOKEN_TYPE, String.class);
	}

	private String generateToken(String email, Role role, long expirationMs, String tokenType) {
		Date now = new Date();
		Date expiration = new Date(now.getTime() + expirationMs);

		return Jwts.builder()
				.subject(email)
				.claim(CLAIM_TOKEN_TYPE, tokenType)
				.claim(CLAIM_ROLE, role.name())
				.issuedAt(now)
				.expiration(expiration)
				.signWith(secretKey)
				.compact();
	}

	private boolean isTokenExpired(String token) {
		return parseClaims(token).getExpiration().before(new Date());
	}

	private Claims parseClaims(String token) {
		return Jwts.parser()
				.verifyWith(secretKey)
				.build()
				.parseSignedClaims(token)
				.getPayload();
	}
}
