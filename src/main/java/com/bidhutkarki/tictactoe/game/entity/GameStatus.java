package com.bidhutkarki.tictactoe.game.entity;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public enum GameStatus {
    WAITING_FOR_OPPONENT,
    READY,
    IN_PROGRESS,
    X_WON,
    O_WON,
    DRAW;

    private static final Map<GameStatus, Set<GameStatus>> TRANSITIONS = new EnumMap<>(GameStatus.class);

    static {
        TRANSITIONS.put(WAITING_FOR_OPPONENT, EnumSet.of(READY));
        TRANSITIONS.put(READY, EnumSet.of(IN_PROGRESS));
        TRANSITIONS.put(IN_PROGRESS, EnumSet.of(IN_PROGRESS, X_WON, O_WON, DRAW));
        TRANSITIONS.put(X_WON, EnumSet.noneOf(GameStatus.class));
        TRANSITIONS.put(O_WON, EnumSet.noneOf(GameStatus.class));
        TRANSITIONS.put(DRAW, EnumSet.noneOf(GameStatus.class));
    }

    public boolean canTransitionTo(GameStatus target) {
        return TRANSITIONS.get(this).contains(target);
    }

    public boolean isTerminal() {
        return TRANSITIONS.get(this).isEmpty();
    }
}
