package com.bidhutkarki.tictactoe.game.service;

import com.bidhutkarki.tictactoe.game.dto.CreateGameRequest;
import com.bidhutkarki.tictactoe.game.dto.GameResponse;
import com.bidhutkarki.tictactoe.game.dto.UpdateGameRequest;
import com.bidhutkarki.tictactoe.game.entity.Game;
import com.bidhutkarki.tictactoe.game.exception.GameNotFoundException;
import com.bidhutkarki.tictactoe.game.repository.GameRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GameService {

    private final GameRepository gameRepository;

    public GameService(GameRepository gameRepository) {
        this.gameRepository = gameRepository;
    }

    @Transactional
    public GameResponse create(CreateGameRequest request) {
        Game saved = gameRepository.save(new Game(request.playerXId(), request.playerOId()));
        return GameResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<GameResponse> findAll() {
        return gameRepository.findAll().stream()
                .map(GameResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public GameResponse findById(Long id) {
        Game game = gameRepository.findById(id)
                .orElseThrow(() -> new GameNotFoundException(id));
        return GameResponse.from(game);
    }

    @Transactional
    public GameResponse update(Long id, UpdateGameRequest request) {
        Game game = gameRepository.findById(id)
                .orElseThrow(() -> new GameNotFoundException(id));
        game.update(request.board(), request.status());
        return GameResponse.from(game);
    }

    @Transactional
    public void delete(Long id) {
        if (!gameRepository.existsById(id)) {
            throw new GameNotFoundException(id);
        }
        gameRepository.deleteById(id);
    }
}
