package com.example.demo.dto;

import java.time.Instant;

import com.example.demo.model.FoodPost;

/**
 * What the browser sees for a post. Contact details are only filled in once the
 * post is claimed: the donor then sees the shelter, and the shelter sees the donor.
 */
public record PostView(
		Long id,
		String description,
		String quantity,
		Instant expiresAt,
		Instant createdAt,
		double latitude,
		double longitude,
		Double distanceKm,
		String status,
		String donorName,
		ContactView donorContact,
		ContactView shelterContact,
		Instant claimedAt) {

	public static String statusOf(FoodPost post) {
		if (post.getClaimedBy() != null) {
			return "CLAIMED";
		}
		return post.getExpiresAt().isAfter(Instant.now()) ? "OPEN" : "EXPIRED";
	}

	/** View for the donor who created the post. */
	public static PostView forDonor(FoodPost post) {
		return new PostView(post.getId(), post.getDescription(), post.getQuantity(), post.getExpiresAt(),
				post.getCreatedAt(), post.getLatitude(), post.getLongitude(), null, statusOf(post),
				post.getDonor().getName(), null, ContactView.of(post.getClaimedBy()), post.getClaimedAt());
	}

	/** View for a shelter; donor contact is only shown to the shelter that claimed it. */
	public static PostView forShelter(FoodPost post, Double distanceKm, boolean claimedByViewer) {
		return new PostView(post.getId(), post.getDescription(), post.getQuantity(), post.getExpiresAt(),
				post.getCreatedAt(), post.getLatitude(), post.getLongitude(), distanceKm, statusOf(post),
				post.getDonor().getName(), claimedByViewer ? ContactView.of(post.getDonor()) : null,
				null, post.getClaimedAt());
	}

	/** View for the admin: both sides' contact details. */
	public static PostView forAdmin(FoodPost post) {
		return new PostView(post.getId(), post.getDescription(), post.getQuantity(), post.getExpiresAt(),
				post.getCreatedAt(), post.getLatitude(), post.getLongitude(), null, statusOf(post),
				post.getDonor().getName(), ContactView.of(post.getDonor()), ContactView.of(post.getClaimedBy()),
				post.getClaimedAt());
	}
}
