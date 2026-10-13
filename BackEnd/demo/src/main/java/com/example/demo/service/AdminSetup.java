package com.example.demo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import com.example.demo.model.AppUser;
import com.example.demo.model.Role;
import com.example.demo.repository.UserRepository;

/** Creates the admin account on startup (email and password come from application.properties). */
@Component
public class AdminSetup implements CommandLineRunner {

	private final UserRepository users;
	private final String email;
	private final String password;

	public AdminSetup(UserRepository users, @Value("${admin.email}") String email,
			@Value("${admin.password}") String password) {
		this.users = users;
		this.email = email.trim().toLowerCase();
		this.password = password;
	}

	@Override
	public void run(String... args) {
		if (users.existsByEmail(email)) {
			return;
		}
		AppUser admin = new AppUser();
		admin.setName("Admin");
		admin.setEmail(email);
		admin.setPasswordHash(new BCryptPasswordEncoder().encode(password));
		admin.setPhone("-");
		admin.setRole(Role.ADMIN);
		users.save(admin);
		System.out.println("Admin account created: " + email);
	}
}
