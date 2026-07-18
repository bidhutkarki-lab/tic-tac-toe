package com.bidhutkarki.tictactoe.result;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Derives each player's current Elo rating and win/loss/draw counts by replaying
 * the game_results audit rows in the order they were recorded. This is the
 * authoritative computation used to warm up and rebuild the Redis leaderboard,
 * and as the fallback when Redis is unavailable.
 */
@Component
@RequiredArgsConstructor
public class AuditEloCalculator {

    private final GameResultRepository gameResultRepository;

    @Transactional(readOnly = true)
    public Map<String, PlayerRatingStats> compute() {
        Map<Long, List<GameResult>> byGame = new LinkedHashMap<>();
        for (GameResult result : gameResultRepository.findAllByOrderByIdAsc()) {
            byGame.computeIfAbsent(result.getGameId(), key -> new ArrayList<>()).add(result);
        }

        Map<String, Accumulator> accumulators = new HashMap<>();
        for (List<GameResult> game : byGame.values()) {
            if (game.size() != 2) {
                continue;
            }
            GameResult a = game.get(0);
            GameResult b = game.get(1);
            Accumulator accA = accumulators.computeIfAbsent(a.getPlayerId(), id -> new Accumulator());
            Accumulator accB = accumulators.computeIfAbsent(b.getPlayerId(), id -> new Accumulator());

            int ratingA = accA.rating;
            int ratingB = accB.rating;
            accA.apply(a.getOutcome(),
                    EloRating.updated(ratingA, ratingB, a.getOutcome().actualScore()));
            accB.apply(b.getOutcome(),
                    EloRating.updated(ratingB, ratingA, b.getOutcome().actualScore()));
        }

        Map<String, PlayerRatingStats> stats = new HashMap<>();
        accumulators.forEach((playerId, acc) -> stats.put(playerId,
                new PlayerRatingStats(acc.rating, acc.wins, acc.losses, acc.draws)));
        return stats;
    }

    private static final class Accumulator {
        private int rating = EloRating.INITIAL_RATING;
        private int wins;
        private int losses;
        private int draws;

        private void apply(Outcome outcome, int newRating) {
            this.rating = newRating;
            switch (outcome) {
                case WIN -> wins++;
                case LOSS -> losses++;
                case DRAW -> draws++;
            }
        }
    }
}
