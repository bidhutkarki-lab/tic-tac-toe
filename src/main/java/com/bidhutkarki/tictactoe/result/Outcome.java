package com.bidhutkarki.tictactoe.result;

public enum Outcome {
    WIN(3),
    DRAW(1),
    LOSS(0);

    private final int points;

    Outcome(int points) {
        this.points = points;
    }

    public int points() {
        return points;
    }

    /**
     * The Elo "actual score" for this outcome: win 1.0, draw 0.5, loss 0.0.
     */
    public double actualScore() {
        return switch (this) {
            case WIN -> 1.0;
            case DRAW -> 0.5;
            case LOSS -> 0.0;
        };
    }
}
