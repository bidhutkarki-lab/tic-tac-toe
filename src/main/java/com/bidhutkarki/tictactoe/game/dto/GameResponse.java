package com.bidhutkarki.tictactoe.game.dto;

import com.bidhutkarki.tictactoe.game.entity.Game;
import com.bidhutkarki.tictactoe.game.entity.GameStatus;
import java.time.Instant;

public record GameResponse(
        Long id,
        String playerXId,
        String playerOId,
        String board,
        GameStatus status,
        Instant createdAt) {

    public static GameResponse from(Game game) {
        return new GameResponse(
                game.getId(),
                game.getPlayerXId(),
                game.getPlayerOId(),
                game.getBoard(),
                game.getStatus(),
                game.getCreatedAt());
    }
}
