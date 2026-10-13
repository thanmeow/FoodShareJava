package com.example.demo.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * Pushes live events to logged-in browsers using Server-Sent Events (built into
 * Spring MVC). The pages turn these events into browser notifications.
 */
@Service
public class NotificationService {

	private final Map<Long, List<SseEmitter>> emittersByUser = new ConcurrentHashMap<>();

	public SseEmitter subscribe(Long userId) {
		SseEmitter emitter = new SseEmitter(0L); // 0 = never time out
		emittersByUser.computeIfAbsent(userId, id -> new CopyOnWriteArrayList<>()).add(emitter);

		emitter.onCompletion(() -> remove(userId, emitter));
		emitter.onTimeout(() -> remove(userId, emitter));
		emitter.onError(e -> remove(userId, emitter));

		sendTo(userId, emitter, "connected", Map.of("userId", userId));
		return emitter;
	}

	public void send(Long userId, String eventName, Object data) {
		for (SseEmitter emitter : emittersByUser.getOrDefault(userId, List.of())) {
			sendTo(userId, emitter, eventName, data);
		}
	}

	public void sendToAll(String eventName, Object data) {
		emittersByUser.keySet().forEach(userId -> send(userId, eventName, data));
	}

	private void sendTo(Long userId, SseEmitter emitter, String eventName, Object data) {
		try {
			emitter.send(SseEmitter.event().name(eventName).data(data));
		}
		catch (IOException | IllegalStateException e) {
			// The browser tab was closed; forget this connection.
			remove(userId, emitter);
		}
	}

	private void remove(Long userId, SseEmitter emitter) {
		List<SseEmitter> emitters = emittersByUser.get(userId);
		if (emitters != null) {
			emitters.remove(emitter);
		}
	}
}
