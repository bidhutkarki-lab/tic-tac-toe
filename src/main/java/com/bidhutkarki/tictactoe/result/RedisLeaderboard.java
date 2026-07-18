package com.bidhutkarki.tictactoe.result;

import com.bidhutkarki.tictactoe.result.dto.LeaderboardEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Component;

/**
 * Redis-backed Elo leaderboard. Ratings live in a sorted set keyed by player id
 * (score = rating) for O(log N) upserts and ranked reads; win/loss/draw counts
 * live in a per-player hash. The game_results audit table remains the source of
 * truth: this cache is warmed on startup and can be rebuilt from it at any time.
 */
@Component
@ConditionalOnProperty(name = "game.redis.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class RedisLeaderboard {

    static final String RATINGS_KEY = "leaderboard:ratings";
    static final String STATS_PREFIX = "leaderboard:stats:";
    private static final String WINS = "wins";
    private static final String LOSSES = "losses";
    private static final String DRAWS = "draws";

    private final StringRedisTemplate redis;
    private final AuditEloCalculator calculator;

    @EventListener(ApplicationReadyEvent.class)
    public void warmUpOnStartup() {
        try {
            rebuild();
            log.info("Warmed up Redis leaderboard from audit history");
        } catch (RuntimeException e) {
            log.warn("Could not warm up Redis leaderboard on startup; will fall back to audit replay", e);
        }
    }

    /** Rebuilds the entire cache from the audit log, replacing any existing data. */
    public void rebuild() {
        Set<String> statKeys = redis.keys(STATS_PREFIX + "*");
        if (statKeys != null && !statKeys.isEmpty()) {
            redis.delete(statKeys);
        }
        redis.delete(RATINGS_KEY);

        Map<String, PlayerRatingStats> stats = calculator.compute();
        HashOperations<String, String, String> hashOps = redis.opsForHash();
        stats.forEach((playerId, s) -> {
            redis.opsForZSet().add(RATINGS_KEY, playerId, s.rating());
            String key = STATS_PREFIX + playerId;
            hashOps.put(key, WINS, String.valueOf(s.wins()));
            hashOps.put(key, LOSSES, String.valueOf(s.losses()));
            hashOps.put(key, DRAWS, String.valueOf(s.draws()));
        });
    }

    /** Applies one finished game's Elo change to both players incrementally. */
    public void recordGame(String playerXId, Outcome outcomeX, String playerOId, Outcome outcomeO) {
        int ratingX = currentRating(playerXId);
        int ratingO = currentRating(playerOId);
        redis.opsForZSet().add(RATINGS_KEY, playerXId,
                EloRating.updated(ratingX, ratingO, outcomeX.actualScore()));
        redis.opsForZSet().add(RATINGS_KEY, playerOId,
                EloRating.updated(ratingO, ratingX, outcomeO.actualScore()));
        incrementCount(playerXId, outcomeX);
        incrementCount(playerOId, outcomeO);
    }

    /** Returns the ranked leaderboard, resolving display names via the given map. */
    public List<LeaderboardEntry> read(Map<String, String> usernames) {
        Set<TypedTuple<String>> tuples = redis.opsForZSet().reverseRangeWithScores(RATINGS_KEY, 0, -1);
        List<LeaderboardEntry> entries = new ArrayList<>();
        if (tuples != null) {
            HashOperations<String, String, String> hashOps = redis.opsForHash();
            for (TypedTuple<String> tuple : tuples) {
                String playerId = tuple.getValue();
                if (playerId == null || tuple.getScore() == null) {
                    continue;
                }
                int rating = (int) Math.round(tuple.getScore());
                String key = STATS_PREFIX + playerId;
                int wins = parse(hashOps.get(key, WINS));
                int losses = parse(hashOps.get(key, LOSSES));
                int draws = parse(hashOps.get(key, DRAWS));
                entries.add(new LeaderboardEntry(0, playerId,
                        usernames.getOrDefault(playerId, playerId),
                        rating, wins, losses, draws, wins + losses + draws));
            }
        }
        return LeaderboardRanker.rank(entries);
    }

    private int currentRating(String playerId) {
        Double score = redis.opsForZSet().score(RATINGS_KEY, playerId);
        return score == null ? EloRating.INITIAL_RATING : (int) Math.round(score);
    }

    private void incrementCount(String playerId, Outcome outcome) {
        redis.opsForHash().increment(STATS_PREFIX + playerId, fieldFor(outcome), 1);
    }

    private String fieldFor(Outcome outcome) {
        return switch (outcome) {
            case WIN -> WINS;
            case LOSS -> LOSSES;
            case DRAW -> DRAWS;
        };
    }

    private int parse(String value) {
        return value == null ? 0 : Integer.parseInt(value);
    }
}
