package com.bidhutkarki.tictactoe.player.service;

import com.bidhutkarki.tictactoe.player.dto.PlayerResponse;
import com.bidhutkarki.tictactoe.player.dto.RegisterPlayerRequest;
import com.bidhutkarki.tictactoe.player.entity.Player;
import com.bidhutkarki.tictactoe.player.exception.UsernameAlreadyExistsException;
import com.bidhutkarki.tictactoe.player.repository.PlayerRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PlayerService {

    private final PlayerRepository playerRepository;

    public PlayerService(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    @Transactional
    public PlayerResponse register(RegisterPlayerRequest request) {
        String username = request.username().trim();
        if (playerRepository.existsByUsernameIgnoreCase(username)) {
            throw new UsernameAlreadyExistsException(username);
        }
        Player saved = playerRepository.save(new Player(username));
        return PlayerResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<PlayerResponse> findAll() {
        return playerRepository.findAll().stream()
                .map(PlayerResponse::from)
                .toList();
    }
}
