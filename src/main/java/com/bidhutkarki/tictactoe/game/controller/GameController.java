package com.bidhutkarki.tictactoe.game.controller;

import com.bidhutkarki.tictactoe.game.dto.CreateGameRequest;
import com.bidhutkarki.tictactoe.game.dto.GameResponse;
import com.bidhutkarki.tictactoe.game.dto.MakeMoveRequest;
import com.bidhutkarki.tictactoe.game.dto.UpdateGameRequest;
import com.bidhutkarki.tictactoe.game.service.GameService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/games")
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;

    @PostMapping
    public ResponseEntity<GameResponse> create(@Valid @RequestBody CreateGameRequest request) {
        GameResponse response = gameService.create(request);
        return ResponseEntity
                .created(URI.create("/games/" + response.id()))
                .body(response);
    }

    @GetMapping
    public List<GameResponse> listGames() {
        return gameService.findAll();
    }

    @GetMapping("/{id}")
    public GameResponse getGame(@PathVariable Long id) {
        return gameService.findById(id);
    }

    @PutMapping("/{id}")
    public GameResponse updateGame(@PathVariable Long id, @Valid @RequestBody UpdateGameRequest request) {
        return gameService.update(id, request);
    }

    @PostMapping("/{id}/moves")
    public GameResponse makeMove(@PathVariable Long id, @Valid @RequestBody MakeMoveRequest request) {
        return gameService.makeMove(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGame(@PathVariable Long id) {
        gameService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
