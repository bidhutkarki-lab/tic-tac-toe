package com.bidhutkarki.tictactoe.game.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record UpdateGameRequest(
        @NotNull(message = "board is required")
        @Pattern(regexp = "^[XO-]{9}$",
                message = "board must be 9 characters using only 'X', 'O', or '-'")
        String board) {
}
