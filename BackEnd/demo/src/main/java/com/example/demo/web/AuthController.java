package com.example.demo.web;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.dto.AuthResponse;
import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.dto.UserView;
import com.example.demo.model.AppUser;
import com.example.demo.model.Role;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.JwtService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final UserRepository users;
	private final CurrentUser currentUser;
	private final JwtService jwtService;
	private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

	public AuthController(UserRepository users, CurrentUser currentUser, JwtService jwtService) {
		this.users = users;
		this.currentUser = currentUser;
		this.jwtService = jwtService;
	}

	@PostMapping("/register")
	@ResponseStatus(HttpStatus.CREATED)
	public AuthResponse register(@RequestBody RegisterRequest request) {
		if (isBlank(request.name()) || isBlank(request.email()) || isBlank(request.phone())
				|| request.role() == null) {
			throw badRequest("Name, email, phone and account type are required");
		}
		if (request.role() == Role.ADMIN) {
			throw badRequest("You can't register as an admin");
		}
		if (request.password() == null || request.password().length() < 6) {
			throw badRequest("Password must be at least 6 characters");
		}
		if (request.role() == Role.SHELTER && (request.latitude() == null || request.longitude() == null)) {
			throw badRequest("Shelters need a location - click \"Detect my location\"");
		}
		String email = request.email().trim().toLowerCase();
		if (users.existsByEmail(email)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "An account with this email already exists");
		}

		AppUser user = new AppUser();
		user.setName(request.name().trim());
		user.setEmail(email);
		user.setPasswordHash(passwordEncoder.encode(request.password()));
		user.setPhone(request.phone().trim());
		user.setAddress(request.address() == null ? null : request.address().trim());
		user.setRole(request.role());
		user.setLatitude(request.latitude());
		user.setLongitude(request.longitude());
		user = users.save(user);

		return new AuthResponse(jwtService.createToken(user), UserView.of(user));
	}

	@PostMapping("/login")
	public AuthResponse login(@RequestBody LoginRequest request) {
		String email = request.email() == null ? "" : request.email().trim().toLowerCase();
		AppUser user = users.findByEmail(email)
				.filter(u -> request.password() != null && passwordEncoder.matches(request.password(), u.getPasswordHash()))
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Wrong email or password"));

		return new AuthResponse(jwtService.createToken(user), UserView.of(user));
	}

	@GetMapping("/me")
	public UserView me(HttpServletRequest request) {
		return UserView.of(currentUser.require(request));
	}

	private static boolean isBlank(String value) {
		return value == null || value.isBlank();
	}

	private static ResponseStatusException badRequest(String message) {
		return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
	}
}
