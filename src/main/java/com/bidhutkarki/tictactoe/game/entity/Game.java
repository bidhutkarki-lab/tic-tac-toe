package com.bidhutkarki.tictactoe.game.entity;

import com.bidhutkarki.tictactoe.game.exception.InvalidGameStateException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "games")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, updatable = false, length = 32)
    private String playerXId;

    @Column(length = 32)
    private String playerOId;

    @Column(nullable = false, length = 9)
    private String board;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GameStatus status;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Version
    private Long version;

    public Game(String playerXId) {
        this.playerXId = playerXId;
        this.board = Board.EMPTY;
        this.status = GameStatus.WAITING_FOR_OPPONENT;
        this.createdAt = Instant.now();
    }

    public void join(String playerOId) {
        transitionTo(GameStatus.READY);
        this.playerOId = playerOId;
    }

    public void start() {
        transitionTo(GameStatus.IN_PROGRESS);
    }

    public void update(String board) {
        transitionTo(new Board(board).status());
        this.board = board;
    }

    private void transitionTo(GameStatus target) {
        if (!status.canTransitionTo(target)) {
            throw new InvalidGameStateException(
                    "game '" + id + "' cannot transition from " + status + " to " + target);
        }
        this.status = target;
    }
}
