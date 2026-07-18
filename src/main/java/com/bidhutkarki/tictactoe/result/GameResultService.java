package com.bidhutkarki.tictactoe.result;

import com.bidhutkarki.tictactoe.game.entity.Game;
import com.bidhutkarki.tictactoe.game.entity.GameStatus;
import com.bidhutkarki.tictactoe.result.dto.GameResultResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class GameResultService {

    private final GameResultRepository gameResultRepository;
    private final ObjectProvider<RedisLeaderboard> redisLeaderboard;

    /**
     * Writes an audit row per player for a finished game. Must be called within
     * an active transaction.
     */
    public void recordResult(Game game) {
        GameStatus status = game.getStatus();
        Outcome outcomeX = outcomeForX(status);
        Outcome outcomeO = outcomeForO(status);
        gameResultRepository.save(new GameResult(game.getId(), game.getPlayerXId(), outcomeX));
        gameResultRepository.save(new GameResult(game.getId(), game.getPlayerOId(), outcomeO));

        RedisLeaderboard redis = redisLeaderboard.getIfAvailable();
        if (redis != null) {
            try {
                redis.recordGame(game.getPlayerXId(), outcomeX, game.getPlayerOId(), outcomeO);
            } catch (RuntimeException e) {
                log.warn("Failed to update Redis leaderboard for game {}; "
                        + "cache will be corrected on next rebuild", game.getId(), e);
            }
        }
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
