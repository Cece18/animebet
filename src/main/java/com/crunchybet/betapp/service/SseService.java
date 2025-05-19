package com.crunchybet.betapp.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class SseService {
    private final Map<String, List<SseEmitter>> emitters = new ConcurrentHashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(SseService.class);

    public SseEmitter createEmitter(String email) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);

        // Add emitter to list for this user
        emitters.computeIfAbsent(email, e -> new CopyOnWriteArrayList<>()).add(emitter);

        emitter.onCompletion(() -> {
            removeEmitter(email, emitter);
            logger.info("SSE connection closed for user: {}", email);
        });

        emitter.onTimeout(() -> {
            removeEmitter(email, emitter);
            logger.info("SSE connection timed out for user: {}", email);
        });

        emitter.onError((e) -> {
            removeEmitter(email, emitter);
            logger.warn("SSE connection error for user {}: {}", email, e.getMessage());
        });

        logger.info("New SSE connection established for user: {}", email);
        return emitter;
    }

    public void sendPointsUpdate(String email, int points, String reason) {
        List<SseEmitter> userEmitters = emitters.get(email);
        if (userEmitters != null && !userEmitters.isEmpty()) {
            logger.info("Found {} emitters for user {}, attempting to send update", userEmitters.size(), email);

            for (SseEmitter emitter : new CopyOnWriteArrayList<>(userEmitters)) {
                try {
                    emitter.send(SseEmitter.event()
                            .name("points-update")
                            .data(Map.of(
                                    "points", points,
                                    "reason", reason
                            )));
                    logger.info("Points update sent to {} ({} points)", email, points);
                } catch (IOException e) {
                    userEmitters.remove(emitter);
                    logger.error("Failed to send points update to {} — emitter removed: {}", email, e.getMessage());
                }
            }
        } else {
            logger.warn("No SSE emitters found for user {}", email);
        }
    }

    private void removeEmitter(String email, SseEmitter emitter) {
        List<SseEmitter> userEmitters = emitters.get(email);
        if (userEmitters != null) {
            userEmitters.remove(emitter);
            if (userEmitters.isEmpty()) {
                emitters.remove(email);
            }
        }
    }
}
