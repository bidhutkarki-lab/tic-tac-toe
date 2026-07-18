package com.bidhutkarki.tictactoe.result;

public final class EloRating {

    public static final int INITIAL_RATING = 1200;
    private static final int K_FACTOR = 32;

    private EloRating() {
    }

    /**
     * @param rating         the player's current rating
     * @param opponentRating the opponent's current rating
     * @param actualScore    1.0 win, 0.5 draw, 0.0 loss
     * @return the player's new rating
     */
    public static int updated(int rating, int opponentRating, double actualScore) {
        double expected = 1.0 / (1.0 + Math.pow(10.0, (opponentRating - rating) / 400.0));
        return (int) Math.round(rating + K_FACTOR * (actualScore - expected));
    }
}
