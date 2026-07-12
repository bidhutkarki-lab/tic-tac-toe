package com.bidhutkarki.tictactoe.game.dto;

import jakarta.validation.constraints.NotNull;

public record CreateGameRequest(
        @NotNull(message = "playerXId is required")
        Long playerXId) {
}
