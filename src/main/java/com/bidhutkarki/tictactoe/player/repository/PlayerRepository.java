package com.bidhutkarki.tictactoe.player.repository;

import com.bidhutkarki.tictactoe.player.entity.Player;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlayerRepository extends JpaRepository<Player, Long> {

    boolean existsByUsernameIgnoreCase(String username);

    Optional<Player> findByUsernameIgnoreCase(String username);
}
