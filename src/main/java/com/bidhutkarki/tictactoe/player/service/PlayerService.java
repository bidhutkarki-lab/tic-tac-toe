package com.bidhutkarki.tictactoe.player.service;

import com.bidhutkarki.tictactoe.player.dto.PlayerResponse;
import com.bidhutkarki.tictactoe.player.dto.RegisterPlayerRequest;
import com.bidhutkarki.tictactoe.player.entity.Player;
import com.bidhutkarki.tictactoe.player.exception.PlayerAlreadyRegisteredException;
import com.bidhutkarki.tictactoe.player.exception.PlayerNotFoundException;
import com.bidhutkarki.tictactoe.player.exception.UsernameAlreadyExistsException;
import com.bidhutkarki.tictactoe.player.repository.PlayerRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class PlayerService {

    private final PlayerRepository playerRepository;

    @Transactional
    public PlayerResponse register(String authId, RegisterPlayerRequest request) {
        String username = request.username().trim();
        if (playerRepository.existsByUsernameIgnoreCase(username)) {
            throw new UsernameAlreadyExistsException(username);
        }
        if (playerRepository.existsByAuthId(authId)) {
            throw new PlayerAlreadyRegisteredException(authId);
        }
        Player saved = playerRepository.save(new Player(username, authId));
        log.info("Registered player id={} username={} authId={}",
                saved.getId(), saved.getUsername(), authId);
        return PlayerResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<PlayerResponse> findAll() {
        return playerRepository.findAll().stream()
                .map(PlayerResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public PlayerResponse findByUserId(String authId) {
        log.info("Looking up player for authId={}", authId);
        return playerRepository.findByAuthId(authId)
                .map(PlayerResponse::from)
                .orElseThrow(() -> new PlayerNotFoundException(
                        "no player found for user '" + authId + "'"));
    }
}
