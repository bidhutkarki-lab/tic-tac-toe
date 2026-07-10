package com.bidhutkarki.tictactoe.user.dto;

import com.bidhutkarki.tictactoe.user.entity.Profile;
import java.time.Instant;

public record ProfileResponse(
        Long id,
        String username,
        String firstName,
        String lastName,
        Instant createdAt,
        Instant updatedAt,
        Long authId) {

    public static ProfileResponse from(Profile profile) {
        return new ProfileResponse(
                profile.getId(),
                profile.getUsername(),
                profile.getFirstName(),
                profile.getLastName(),
                profile.getCreatedAt(),
                profile.getUpdatedAt(),
                profile.getAuthId());
    }
}
