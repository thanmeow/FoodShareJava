package com.example.demo.service;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.demo.model.AppUser;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/** Creates and checks the login tokens sent by the browser. */
@Service
public class JwtService {

	private final SecretKey key;
	private final long expiryMillis;

	public JwtService(@Value("${jwt.secret}") String secret, @Value("${jwt.expiry-hours}") long expiryHours) {
		this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
		this.expiryMillis = expiryHours * 60 * 60 * 1000;
	}

	public String createToken(AppUser user) {
		Date now = new Date();
		return Jwts.builder()
				.subject(String.valueOf(user.getId()))
				.claim("role", user.getRole().name())
				.issuedAt(now)
				.expiration(new Date(now.getTime() + expiryMillis))
				.signWith(key)
				.compact();
	}

	/** Returns the user id inside the token, or null if the token is missing, invalid or expired. */
	public Long getUserId(String token) {
		if (token == null || token.isBlank()) {
			return null;
		}
		try {
			String subject = Jwts.parser().verifyWith(key).build()
					.parseSignedClaims(token)
					.getPayload()
					.getSubject();
			return Long.valueOf(subject);
		}
		catch (JwtException | IllegalArgumentException e) {
			return null;
		}
	}
}
