package com.bidhutkarki.tictactoe.game.event;

import com.bidhutkarki.tictactoe.game.dto.GameResponse;

public record GameEvent(Long gameId, GameResponse game) {}
