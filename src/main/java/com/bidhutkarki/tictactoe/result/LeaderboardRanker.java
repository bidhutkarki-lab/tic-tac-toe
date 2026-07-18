package com.bidhutkarki.tictactoe.result;

import com.bidhutkarki.tictactoe.result.dto.LeaderboardEntry;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class LeaderboardRanker {

    private LeaderboardRanker() {
    }

    /**
     * Sorts entries by rating descending (username as tie-breaker) and assigns
     * ranks, where equal ratings share the same rank (1, 2, 2, 4, ...).
     */
    public static List<LeaderboardEntry> rank(List<LeaderboardEntry> entries) {
        List<LeaderboardEntry> sorted = entries.stream()
                .sorted(Comparator.comparingInt(LeaderboardEntry::rating).reversed()
                        .thenComparing(LeaderboardEntry::username, String.CASE_INSENSITIVE_ORDER))
                .toList();

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
}
