package com.bidhutkarki.tictactoe.game.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record MakeMoveRequest(
        @NotNull(message = "playerId is required")
        Long playerId,
        @NotNull(message = "cell is required")
        @Min(value = 0, message = "cell must be between 0 and 8")
        @Max(value = 8, message = "cell must be between 0 and 8")
        Integer cell) {
}
