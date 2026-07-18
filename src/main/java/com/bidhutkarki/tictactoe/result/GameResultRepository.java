package com.bidhutkarki.tictactoe.result;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameResultRepository extends JpaRepository<GameResult, Long> {

    List<GameResult> findByPlayerIdOrderByCreatedAtDesc(String playerId);

    List<GameResult> findByGameId(Long gameId);

    List<GameResult> findAllByOrderByIdAsc();
}
