package com.bidhutkarki.tictactoe.game.entity;

public final class Board {

    public static final String EMPTY = "---------";
    public static final char MARK_X = 'X';
    public static final char MARK_O = 'O';

    private static final char EMPTY_CELL = '-';
    private static final int CELL_COUNT = 9;
    private static final int[][] WINNING_LINES = {
        {0, 1, 2}, {3, 4, 5}, {6, 7, 8}, // rows
        {0, 3, 6}, {1, 4, 7}, {2, 5, 8}, // columns
        {0, 4, 8}, {2, 4, 6} // diagonals
    };

    private final char[] cells;

    public Board(String board) {
        if (board == null || board.length() != CELL_COUNT) {
            throw new IllegalArgumentException("board must be " + CELL_COUNT + " characters");
        }
        this.cells = board.toCharArray();
    }

    public GameStatus status() {
        char winner = winner();
        if (winner == MARK_X) {
            return GameStatus.X_WON;
        }
        if (winner != EMPTY_CELL) {
            return GameStatus.O_WON;
        }
        return isFull() ? GameStatus.DRAW : GameStatus.IN_PROGRESS;
    }

    public char nextMark() {
        // X always moves first, so X plays whenever the marks are level.
        return count(MARK_X) <= count(MARK_O) ? MARK_X : MARK_O;
    }

    public boolean isEmptyAt(int cell) {
        return cells[cell] == EMPTY_CELL;
    }

    public String withMark(int cell, char mark) {
        char[] updated = cells.clone();
        updated[cell] = mark;
        return new String(updated);
    }

    private char winner() {
        for (int[] line : WINNING_LINES) {
            char first = cells[line[0]];
            if (first != EMPTY_CELL && first == cells[line[1]] && first == cells[line[2]]) {
                return first;
            }
        }
        return EMPTY_CELL;
    }

    private boolean isFull() {
        return count(EMPTY_CELL) == 0;
    }

    private int count(char mark) {
        int total = 0;
        for (char cell : cells) {
            if (cell == mark) {
                total++;
            }
        }
        return total;
    }
}
