package com.bidhutkarki.tictactoe.game.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "games")
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, updatable = false)
    private Long playerXId;

    @Column(nullable = false, updatable = false)
    private Long playerOId;

    @Column(nullable = false, length = 9)
    private String board;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GameStatus status;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected Game() {
        // for JPA
    }

    public Game(Long playerXId, Long playerOId) {
        this.playerXId = playerXId;
        this.playerOId = playerOId;
        this.board = Board.EMPTY;
        this.status = GameStatus.IN_PROGRESS;
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Long getPlayerXId() {
        return playerXId;
    }

    public Long getPlayerOId() {
        return playerOId;
    }

    public String getBoard() {
        return board;
    }

    public GameStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void update(String board) {
        this.board = board;
        this.status = new Board(board).status();
    }
}
