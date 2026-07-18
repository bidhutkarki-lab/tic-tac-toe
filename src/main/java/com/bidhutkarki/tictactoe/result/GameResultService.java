package com.bidhutkarki.tictactoe.result;

import com.bidhutkarki.tictactoe.game.entity.Game;
import com.bidhutkarki.tictactoe.game.entity.GameStatus;
import com.bidhutkarki.tictactoe.result.dto.GameResultResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GameResultService {

    private final GameResultRepository gameResultRepository;

    /**
     * Writes an audit row per player for a finished game. Must be called within
     * an active transaction.
     */
    public void recordResult(Game game) {
        GameStatus status = game.getStatus();
        gameResultRepository.save(
                new GameResult(game.getId(), game.getPlayerXId(), outcomeForX(status)));
        gameResultRepository.save(
                new GameResult(game.getId(), game.getPlayerOId(), outcomeForO(status)));
    }

    @Transactional(readOnly = true)
    public List<GameResultResponse> findAll() {
        return gameResultRepository.findAll().stream()
                .map(GameResultResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<GameResultResponse> findByPlayer(String playerId) {
        return gameResultRepository.findByPlayerIdOrderByCreatedAtDesc(playerId).stream()
                .map(GameResultResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<GameResultResponse> findByGame(Long gameId) {
        return gameResultRepository.findByGameId(gameId).stream()
                .map(GameResultResponse::from)
                .toList();
    }

    private Outcome outcomeForX(GameStatus status) {
        return switch (status) {
            case X_WON -> Outcome.WIN;
            case O_WON -> Outcome.LOSS;
            case DRAW -> Outcome.DRAW;
            default -> throw new IllegalArgumentException(
                    "cannot record result for non-terminal game status " + status);
        };
    }

    private Outcome outcomeForO(GameStatus status) {
        return switch (status) {
            case X_WON -> Outcome.LOSS;
            case O_WON -> Outcome.WIN;
            case DRAW -> Outcome.DRAW;
            default -> throw new IllegalArgumentException(
                    "cannot record result for non-terminal game status " + status);
        };
    }
}
