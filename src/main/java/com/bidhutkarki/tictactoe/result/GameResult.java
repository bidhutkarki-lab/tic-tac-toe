package com.bidhutkarki.tictactoe.result;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "game_results")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GameResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, updatable = false)
    private Long gameId;

    @Column(nullable = false, updatable = false, length = 32)
    private String playerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private Outcome outcome;

    @Column(nullable = false, updatable = false)
    private int points;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    public GameResult(Long gameId, String playerId, Outcome outcome) {
        this.gameId = gameId;
        this.playerId = playerId;
        this.outcome = outcome;
        this.points = outcome.points();
        this.createdAt = Instant.now();
    }
}
