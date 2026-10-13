package com.example.demo.web;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.CreatePostRequest;
import com.example.demo.dto.PostView;
import com.example.demo.model.Role;
import com.example.demo.service.FoodPostService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/posts")
public class PostController {

	private final FoodPostService postService;
	private final CurrentUser currentUser;

	public PostController(FoodPostService postService, CurrentUser currentUser) {
		this.postService = postService;
		this.currentUser = currentUser;
	}

	/** Donor: post leftover food. */
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public PostView create(@RequestBody CreatePostRequest body, HttpServletRequest request) {
		return postService.create(currentUser.require(request, Role.DONOR), body);
	}

	/** Donor: my posts, with the claiming shelter's contact once claimed. */
	@GetMapping("/mine")
	public List<PostView> mine(HttpServletRequest request) {
		return postService.donorPosts(currentUser.require(request, Role.DONOR));
	}

	/** Shelter: unclaimed posts within 10 km, nearest first. */
	@GetMapping("/nearby")
	public List<PostView> nearby(HttpServletRequest request) {
		return postService.nearby(currentUser.require(request, Role.SHELTER));
	}

	/** Shelter: posts I have claimed, with the donor's contact. */
	@GetMapping("/claimed")
	public List<PostView> claimed(HttpServletRequest request) {
		return postService.claimedBy(currentUser.require(request, Role.SHELTER));
	}

	/** Shelter: claim a post. Returns 409 if another shelter got there first. */
	@PostMapping("/{id}/claim")
	public PostView claim(@PathVariable Long id, HttpServletRequest request) {
		return postService.claim(currentUser.require(request, Role.SHELTER), id);
	}
}
