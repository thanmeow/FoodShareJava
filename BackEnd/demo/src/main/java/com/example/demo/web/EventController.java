package com.example.demo.web;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.example.demo.service.NotificationService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
public class EventController {

	private final NotificationService notifications;
	private final CurrentUser currentUser;

	public EventController(NotificationService notifications, CurrentUser currentUser) {
		this.notifications = notifications;
		this.currentUser = currentUser;
	}

	/** Live event stream for the logged-in user (new posts, claims, removals). */
	@GetMapping(path = "/api/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public SseEmitter events(HttpServletRequest request) {
		return notifications.subscribe(currentUser.require(request).getId());
	}
}
