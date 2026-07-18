package com.bidhutkarki.tictactoe.game.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GameEventPublisher {

    public static final String CHANNEL = "game-events";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public void publish(GameEvent event) {
        try {
            redisTemplate.convertAndSend(CHANNEL, objectMapper.writeValueAsString(event));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("failed to serialize game event for game " + event.gameId(), e);
        }
    }
}
