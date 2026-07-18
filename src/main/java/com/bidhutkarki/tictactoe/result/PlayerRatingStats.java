package com.bidhutkarki.tictactoe.result;

public record PlayerRatingStats(int rating, int wins, int losses, int draws) {

    public int gamesPlayed() {
        return wins + losses + draws;
    }
}
