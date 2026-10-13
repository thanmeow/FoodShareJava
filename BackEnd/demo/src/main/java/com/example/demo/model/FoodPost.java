package com.example.demo.model;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

@Entity
public class FoodPost {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(optional = false)
	private AppUser donor;

	@Column(nullable = false, length = 1000)
	private String description;

	@Column(nullable = false)
	private String quantity;

	@Column(nullable = false)
	private Instant expiresAt;

	@Column(nullable = false)
	private double latitude;

	@Column(nullable = false)
	private double longitude;

	@Column(nullable = false)
	private Instant createdAt;

	// null until a shelter claims the post
	@ManyToOne
	private AppUser claimedBy;

	private Instant claimedAt;

	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }

	public AppUser getDonor() { return donor; }
	public void setDonor(AppUser donor) { this.donor = donor; }

	public String getDescription() { return description; }
	public void setDescription(String description) { this.description = description; }

	public String getQuantity() { return quantity; }
	public void setQuantity(String quantity) { this.quantity = quantity; }

	public Instant getExpiresAt() { return expiresAt; }
	public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }

	public double getLatitude() { return latitude; }
	public void setLatitude(double latitude) { this.latitude = latitude; }

	public double getLongitude() { return longitude; }
	public void setLongitude(double longitude) { this.longitude = longitude; }

	public Instant getCreatedAt() { return createdAt; }
	public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

	public AppUser getClaimedBy() { return claimedBy; }
	public void setClaimedBy(AppUser claimedBy) { this.claimedBy = claimedBy; }

	public Instant getClaimedAt() { return claimedAt; }
	public void setClaimedAt(Instant claimedAt) { this.claimedAt = claimedAt; }
}
