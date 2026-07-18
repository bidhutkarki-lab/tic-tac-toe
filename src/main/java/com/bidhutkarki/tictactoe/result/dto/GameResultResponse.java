package com.bidhutkarki.tictactoe.result.dto;

import com.bidhutkarki.tictactoe.result.GameResult;
import com.bidhutkarki.tictactoe.result.Outcome;
import java.time.Instant;

public record GameResultResponse(
        Long id,
        Long gameId,
        String playerId,
        Outcome outcome,
        int points,
        Instant createdAt) {

    public static GameResultResponse from(GameResult result) {
        return new GameResultResponse(
                result.getId(),
                result.getGameId(),
                result.getPlayerId(),
                result.getOutcome(),
                result.getPoints(),
                result.getCreatedAt());
    }
}
