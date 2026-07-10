package com.bidhutkarki.tictactoe.user.dto;

import java.time.Instant;
import java.util.Set;

public record UserResponse(
        Long id,
        String email,
        String username,
        Set<String> roles,
        Instant createdAt) {
}
