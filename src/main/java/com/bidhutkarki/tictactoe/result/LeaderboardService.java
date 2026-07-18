package com.bidhutkarki.tictactoe.result;

import com.bidhutkarki.tictactoe.player.entity.Player;
import com.bidhutkarki.tictactoe.player.repository.PlayerRepository;
import com.bidhutkarki.tictactoe.result.dto.LeaderboardEntry;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class LeaderboardService {

    private final AuditEloCalculator calculator;
    private final PlayerRepository playerRepository;
    private final ObjectProvider<RedisLeaderboard> redisLeaderboard;

    @Transactional(readOnly = true)
    public List<LeaderboardEntry> leaderboard() {
        Map<String, String> usernames = usernames();

        RedisLeaderboard redis = redisLeaderboard.getIfAvailable();
        if (redis != null) {
            try {
                return redis.read(usernames);
            } catch (RuntimeException e) {
                log.warn("Redis leaderboard read failed; falling back to audit replay", e);
            }
        }
        return fromAudit(usernames);
    }

    private List<LeaderboardEntry> fromAudit(Map<String, String> usernames) {
        List<LeaderboardEntry> entries = calculator.compute().entrySet().stream()
                .map(entry -> toEntry(entry.getKey(), entry.getValue(),
                        usernames.getOrDefault(entry.getKey(), entry.getKey())))
                .toList();
        return LeaderboardRanker.rank(entries);
    }

    private LeaderboardEntry toEntry(String playerId, PlayerRatingStats stats, String username) {
        return new LeaderboardEntry(0, playerId, username, stats.rating(),
                stats.wins(), stats.losses(), stats.draws(), stats.gamesPlayed());
    }

    private Map<String, String> usernames() {
        return playerRepository.findAll().stream()
                .collect(Collectors.toMap(Player::getId, Player::getUsername));
    }
}
