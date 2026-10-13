package com.example.demo.dto;

import com.example.demo.model.AppUser;
import com.example.demo.model.Role;

public record UserView(
		Long id,
		String name,
		String email,
		String phone,
		String address,
		Role role,
		Double latitude,
		Double longitude) {

	public static UserView of(AppUser user) {
		return new UserView(user.getId(), user.getName(), user.getEmail(), user.getPhone(),
				user.getAddress(), user.getRole(), user.getLatitude(), user.getLongitude());
	}
}
