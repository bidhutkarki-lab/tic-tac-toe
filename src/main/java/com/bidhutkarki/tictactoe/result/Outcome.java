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
}
