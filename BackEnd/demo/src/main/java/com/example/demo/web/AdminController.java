package com.example.demo.web;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.PostView;
import com.example.demo.dto.UserView;
import com.example.demo.model.Role;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.FoodPostService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

	private final UserRepository users;
	private final FoodPostService postService;
	private final CurrentUser currentUser;

	public AdminController(UserRepository users, FoodPostService postService, CurrentUser currentUser) {
		this.users = users;
		this.postService = postService;
		this.currentUser = currentUser;
	}

	@GetMapping("/users")
	public List<UserView> users(HttpServletRequest request) {
		currentUser.require(request, Role.ADMIN);
		return users.findAll().stream().map(UserView::of).toList();
	}

	@GetMapping("/posts")
	public List<PostView> posts(HttpServletRequest request) {
		currentUser.require(request, Role.ADMIN);
		return postService.allPosts();
	}

	@DeleteMapping("/posts/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deletePost(@PathVariable Long id, HttpServletRequest request) {
		currentUser.require(request, Role.ADMIN);
		postService.delete(id);
	}
}
