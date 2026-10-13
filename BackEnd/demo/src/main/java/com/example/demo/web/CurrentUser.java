package com.example.demo.web;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.model.AppUser;
import com.example.demo.model.Role;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.JwtService;

import jakarta.servlet.http.HttpServletRequest;

/** Reads the logged-in user from the JWT sent with the request. */
@Component
public class CurrentUser {

	private final UserRepository users;
	private final JwtService jwtService;

	public CurrentUser(UserRepository users, JwtService jwtService) {
		this.users = users;
		this.jwtService = jwtService;
	}

	public AppUser require(HttpServletRequest request) {
		Long userId = jwtService.getUserId(tokenFrom(request));
		if (userId == null) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please log in");
		}
		return users.findById(userId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please log in"));
	}

	public AppUser require(HttpServletRequest request, Role role) {
		AppUser user = require(request);
		if (user.getRole() != role) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only a " + role.name().toLowerCase() + " can do this");
		}
		return user;
	}

	private static String tokenFrom(HttpServletRequest request) {
		String header = request.getHeader("Authorization");
		if (header != null && header.startsWith("Bearer ")) {
			return header.substring(7);
		}
		// The browser's EventSource can't set headers, so /api/events passes the token in the URL
		return request.getParameter("token");
	}
}
