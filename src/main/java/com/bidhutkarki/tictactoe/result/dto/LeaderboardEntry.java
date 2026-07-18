package com.bidhutkarki.tictactoe.result.dto;

public record LeaderboardEntry(
        int rank,
        String playerId,
        String username,
        int rating,
        int wins,
        int losses,
        int draws,
        int gamesPlayed) {
}
