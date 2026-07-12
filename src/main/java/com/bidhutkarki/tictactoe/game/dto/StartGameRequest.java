package com.bidhutkarki.tictactoe.game.dto;

import jakarta.validation.constraints.NotNull;

public record StartGameRequest(
        @NotNull(message = "playerId is required")
        Long playerId) {
}
