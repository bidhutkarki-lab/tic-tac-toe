package com.bidhutkarki.tictactoe.game.dto;

import jakarta.validation.constraints.NotNull;

public record JoinGameRequest(
        @NotNull(message = "playerId is required")
        String playerId) {
}
