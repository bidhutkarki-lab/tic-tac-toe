package com.bidhutkarki.tictactoe.player.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterPlayerRequest(
        @NotBlank(message = "username is required")
        @Size(min = 3, max = 20, message = "username must be between 3 and 20 characters")
        @Pattern(regexp = "^[A-Za-z0-9_]+$",
                message = "username may only contain letters, digits, and underscores")
        String username,
        @Size(max = 32, message = "profileId must be at most 32 characters")
        String profileId) {
}
