package com.bidhutkarki.tictactoe.player.dto;

import com.bidhutkarki.tictactoe.player.entity.Player;
import java.time.Instant;

public record PlayerResponse(String id, String username, String authId, Instant createdAt) {

    public static PlayerResponse from(Player player) {
        return new PlayerResponse(player.getId(), player.getUsername(), player.getAuthId(), player.getCreatedAt());
    }
}
