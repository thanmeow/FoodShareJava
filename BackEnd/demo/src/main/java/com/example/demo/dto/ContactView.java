package com.example.demo.dto;

import com.example.demo.model.AppUser;

public record ContactView(String name, String phone, String email, String address) {

	public static ContactView of(AppUser user) {
		return user == null ? null
				: new ContactView(user.getName(), user.getPhone(), user.getEmail(), user.getAddress());
	}
}
