package com.example.demo.repository;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.model.AppUser;
import com.example.demo.model.FoodPost;

public interface FoodPostRepository extends JpaRepository<FoodPost, Long> {

	@Query("select p from FoodPost p where p.claimedBy is null and p.expiresAt > :now")
	List<FoodPost> findOpen(@Param("now") Instant now);

	List<FoodPost> findAllByOrderByCreatedAtDesc();

	List<FoodPost> findByDonorIdOrderByCreatedAtDesc(Long donorId);

	List<FoodPost> findByClaimedByIdOrderByClaimedAtDesc(Long shelterId);

	/**
	 * Atomic first-come-first-served claim. The WHERE clause only matches while the
	 * post is still unclaimed, and the database row lock makes concurrent updates run
	 * one after another, so exactly one shelter gets 1 back and everyone else gets 0.
	 */
	@Transactional
	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("""
			update FoodPost p set p.claimedBy = :shelter, p.claimedAt = :now
			where p.id = :id and p.claimedBy is null and p.expiresAt > :now
			""")
	int claim(@Param("id") Long id, @Param("shelter") AppUser shelter, @Param("now") Instant now);
}
