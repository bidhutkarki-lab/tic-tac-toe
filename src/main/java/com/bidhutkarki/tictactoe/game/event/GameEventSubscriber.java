package com.bidhutkarki.tictactoe.game.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class GameEventSubscriber implements MessageListener {

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            GameEvent event = objectMapper.readValue(message.getBody(), GameEvent.class);
            messagingTemplate.convertAndSend("/topic/games/" + event.gameId(), event.game());
        } catch (IOException e) {
            log.error("failed to handle game event from redis", e);
        }
    }
}
