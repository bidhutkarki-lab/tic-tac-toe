package com.bidhutkarki.tictactoe.player.service;

import com.bidhutkarki.tictactoe.player.dto.PlayerResponse;
import com.bidhutkarki.tictactoe.player.dto.RegisterPlayerRequest;
import com.bidhutkarki.tictactoe.player.entity.Player;
import com.bidhutkarki.tictactoe.player.exception.UsernameAlreadyExistsException;
import com.bidhutkarki.tictactoe.player.repository.PlayerRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PlayerService {

    private final PlayerRepository playerRepository;

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
