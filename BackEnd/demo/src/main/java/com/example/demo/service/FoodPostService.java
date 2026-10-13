package com.example.demo.service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.dto.CreatePostRequest;
import com.example.demo.dto.PostView;
import com.example.demo.model.AppUser;
import com.example.demo.model.FoodPost;
import com.example.demo.model.Role;
import com.example.demo.repository.FoodPostRepository;
import com.example.demo.repository.UserRepository;

@Service
public class FoodPostService {

	public static final double RADIUS_KM = 10.0;

	private final FoodPostRepository posts;
	private final UserRepository users;
	private final NotificationService notifications;

	public FoodPostService(FoodPostRepository posts, UserRepository users, NotificationService notifications) {
		this.posts = posts;
		this.users = users;
		this.notifications = notifications;
	}

	public PostView create(AppUser donor, CreatePostRequest request) {
		if (isBlank(request.description()) || isBlank(request.quantity())) {
			throw badRequest("Description and quantity are required");
		}
		if (request.expiresAt() == null || !request.expiresAt().isAfter(Instant.now())) {
			throw badRequest("Expiry time must be in the future");
		}
		if (request.latitude() == null || request.longitude() == null) {
			throw badRequest("Location is required - allow location access in your browser");
		}

		FoodPost post = new FoodPost();
		post.setDonor(donor);
		post.setDescription(request.description().trim());
		post.setQuantity(request.quantity().trim());
		post.setExpiresAt(request.expiresAt());
		post.setLatitude(request.latitude());
		post.setLongitude(request.longitude());
		post.setCreatedAt(Instant.now());
		post = posts.save(post);

		// Alert every shelter within 10 km
		for (AppUser shelter : users.findByRole(Role.SHELTER)) {
			Double distance = distanceKm(shelter, post);
			if (distance != null && distance <= RADIUS_KM) {
				notifications.send(shelter.getId(), "new-post", PostView.forShelter(post, distance, false));
			}
		}
		return PostView.forDonor(post);
	}

	/** Unclaimed, unexpired posts within 10 km of the shelter, nearest first. */
	public List<PostView> nearby(AppUser shelter) {
		return posts.findOpen(Instant.now()).stream()
				.map(post -> PostView.forShelter(post, distanceKm(shelter, post), false))
				.filter(view -> view.distanceKm() != null && view.distanceKm() <= RADIUS_KM)
				.sorted(Comparator.comparing(PostView::distanceKm))
				.toList();
	}

	public List<PostView> donorPosts(AppUser donor) {
		return posts.findByDonorIdOrderByCreatedAtDesc(donor.getId()).stream()
				.map(PostView::forDonor)
				.toList();
	}

	public List<PostView> claimedBy(AppUser shelter) {
		return posts.findByClaimedByIdOrderByClaimedAtDesc(shelter.getId()).stream()
				.map(post -> PostView.forShelter(post, distanceKm(shelter, post), true))
				.toList();
	}

	public PostView claim(AppUser shelter, Long postId) {
		int updated = posts.claim(postId, shelter, Instant.now());
		if (updated == 0) {
			FoodPost post = posts.findById(postId)
					.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));
			String reason = post.getClaimedBy() != null ? "Another shelter already claimed this food"
					: "This post has expired";
			throw new ResponseStatusException(HttpStatus.CONFLICT, reason);
		}

		FoodPost post = posts.findById(postId).orElseThrow();
		// Tell the donor who claimed it, and remove it from every other shelter's feed
		notifications.send(post.getDonor().getId(), "post-claimed", PostView.forDonor(post));
		notifications.sendToAll("post-removed", Map.of("id", postId));
		return PostView.forShelter(post, distanceKm(shelter, post), true);
	}

	/** Admin: every post, newest first. */
	public List<PostView> allPosts() {
		return posts.findAllByOrderByCreatedAtDesc().stream()
				.map(PostView::forAdmin)
				.toList();
	}

	/** Admin: remove a post and take it off every shelter's feed. */
	public void delete(Long postId) {
		if (!posts.existsById(postId)) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found");
		}
		posts.deleteById(postId);
		notifications.sendToAll("post-removed", Map.of("id", postId));
	}

	private static Double distanceKm(AppUser shelter, FoodPost post) {
		if (shelter.getLatitude() == null || shelter.getLongitude() == null) {
			return null;
		}
		return GeoUtils.round(GeoUtils.haversineKm(shelter.getLatitude(), shelter.getLongitude(),
				post.getLatitude(), post.getLongitude()));
	}

	private static boolean isBlank(String value) {
		return value == null || value.isBlank();
	}

	private static ResponseStatusException badRequest(String message) {
		return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
	}
}
