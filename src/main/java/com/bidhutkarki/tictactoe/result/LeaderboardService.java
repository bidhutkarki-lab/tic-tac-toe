package com.bidhutkarki.tictactoe.result;

import com.bidhutkarki.tictactoe.player.entity.Player;
import com.bidhutkarki.tictactoe.player.repository.PlayerRepository;
import com.bidhutkarki.tictactoe.result.dto.LeaderboardEntry;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LeaderboardService {

    private final GameResultRepository gameResultRepository;
    private final PlayerRepository playerRepository;

    @Transactional(readOnly = true)
    public List<LeaderboardEntry> leaderboard() {
        Map<String, PlayerStats> stats = replayElo();
        Map<String, String> usernames = playerRepository.findAll().stream()
                .collect(Collectors.toMap(Player::getId, Player::getUsername));

        List<LeaderboardEntry> ranked = stats.entrySet().stream()
                .map(entry -> toEntry(entry.getKey(), entry.getValue(),
                        usernames.getOrDefault(entry.getKey(), entry.getKey())))
                .sorted(Comparator.comparingInt(LeaderboardEntry::rating).reversed()
                        .thenComparing(LeaderboardEntry::username, String.CASE_INSENSITIVE_ORDER))
                .toList();

        return assignRanks(ranked);
    }

    /**
     * Recomputes every player's Elo rating by replaying the audit rows in the
     * order they were recorded. Each game's two rows are updated together using
     * the ratings held before that game.
     */
    private Map<String, PlayerStats> replayElo() {
        Map<Long, List<GameResult>> byGame = new LinkedHashMap<>();
        for (GameResult result : gameResultRepository.findAllByOrderByIdAsc()) {
            byGame.computeIfAbsent(result.getGameId(), key -> new ArrayList<>()).add(result);
        }

        Map<String, PlayerStats> stats = new HashMap<>();
        for (List<GameResult> game : byGame.values()) {
            if (game.size() != 2) {
                continue;
            }
            GameResult a = game.get(0);
            GameResult b = game.get(1);
            PlayerStats statsA = stats.computeIfAbsent(a.getPlayerId(), id -> new PlayerStats());
            PlayerStats statsB = stats.computeIfAbsent(b.getPlayerId(), id -> new PlayerStats());

            int ratingA = statsA.rating;
            int ratingB = statsB.rating;
            statsA.apply(a.getOutcome(), EloRating.updated(ratingA, ratingB, actualScore(a.getOutcome())));
            statsB.apply(b.getOutcome(), EloRating.updated(ratingB, ratingA, actualScore(b.getOutcome())));
        }
        return stats;
    }

    private double actualScore(Outcome outcome) {
        return switch (outcome) {
            case WIN -> 1.0;
            case DRAW -> 0.5;
            case LOSS -> 0.0;
        };
    }

    private LeaderboardEntry toEntry(String playerId, PlayerStats stats, String username) {
        return new LeaderboardEntry(
                0, playerId, username, stats.rating,
                stats.wins, stats.losses, stats.draws,
                stats.wins + stats.losses + stats.draws);
    }

    private List<LeaderboardEntry> assignRanks(List<LeaderboardEntry> sorted) {
        List<LeaderboardEntry> ranked = new ArrayList<>(sorted.size());
        int rank = 0;
        int position = 0;
        int previousRating = Integer.MIN_VALUE;
        for (LeaderboardEntry entry : sorted) {
            position++;
            if (entry.rating() != previousRating) {
                rank = position;
                previousRating = entry.rating();
            }
            ranked.add(new LeaderboardEntry(
                    rank, entry.playerId(), entry.username(), entry.rating(),
                    entry.wins(), entry.losses(), entry.draws(), entry.gamesPlayed()));
        }
        return ranked;
    }

    private static final class PlayerStats {
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
