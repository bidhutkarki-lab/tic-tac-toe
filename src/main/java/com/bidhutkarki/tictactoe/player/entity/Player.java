package com.bidhutkarki.tictactoe.player.entity;

import com.bidhutkarki.tictactoe.common.UuidGenerator;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "players")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Player {

    @Id
    @Column(length = 32, updatable = false)
    private String id;

    @Column(length = 32, unique = true)
    private String profileId;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    public Player(String username) {
        this(username, null);
    }

    public Player(String username, String profileId) {
        this.id = UuidGenerator.newId();
        this.profileId = profileId;
        this.username = username;
        this.createdAt = Instant.now();
    }
}
