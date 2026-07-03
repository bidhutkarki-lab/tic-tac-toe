package com.bidhutkarki.tictactoe.game.repository;

import com.bidhutkarki.tictactoe.game.entity.Game;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameRepository extends JpaRepository<Game, Long> {
}
