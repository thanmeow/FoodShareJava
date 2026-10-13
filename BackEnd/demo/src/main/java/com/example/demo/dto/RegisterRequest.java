package com.example.demo.dto;

import com.example.demo.model.Role;

public record RegisterRequest(
		String name,
		String email,
		String password,
		String phone,
		String address,
		Role role,
		Double latitude,
		Double longitude) {
}
