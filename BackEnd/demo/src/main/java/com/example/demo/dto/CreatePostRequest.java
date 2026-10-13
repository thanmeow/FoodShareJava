package com.example.demo.dto;

import java.time.Instant;

public record CreatePostRequest(
		String description,
		String quantity,
		Instant expiresAt,
		Double latitude,
		Double longitude) {
}
