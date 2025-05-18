package com.crunchybet.betapp.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SseService {
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(SseService.class);

    public SseEmitter createEmitter(String username) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);

        emitter.onCompletion(() -> {
            emitters.remove(username);
            logger.info("SSE connection closed for user: {}", username);
        });

        emitter.onTimeout(() -> {
            emitters.remove(username);
            logger.info("SSE connection timed out for user: {}", username);
        });

        emitters.put(username, emitter);
        logger.info("New SSE connection established for user: {}", username);
        return emitter;
    }

    public void sendPointsUpdate(String username, int points, String reason) {
        SseEmitter emitter = emitters.get(username);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name("points-update")
                        .data(Map.of(
                                "points", points,
                                "reason", reason
                        )));
                logger.info("Points update sent to user {}: {} ({})", username, points, reason);
            } catch (IOException e) {
                emitters.remove(username);
                logger.error("Error sending points update to user {}: {}", username, e.getMessage());
            }
        }
    }
}