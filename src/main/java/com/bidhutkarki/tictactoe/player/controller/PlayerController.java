package com.bidhutkarki.tictactoe.player.controller;

import com.bidhutkarki.tictactoe.player.dto.PlayerResponse;
import com.bidhutkarki.tictactoe.player.dto.RegisterPlayerRequest;
import com.bidhutkarki.tictactoe.player.service.PlayerService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/players")
@RequiredArgsConstructor
public class PlayerController {

    private final PlayerService playerService;

    @PostMapping
    public ResponseEntity<PlayerResponse> register(@Valid @RequestBody RegisterPlayerRequest request) {
        PlayerResponse response = playerService.register(request);
        return ResponseEntity
                .created(URI.create("/api/players/" + response.id()))
                .body(response);
    }

    @GetMapping
    public List<PlayerResponse> listPlayers() {
        return playerService.findAll();
    }
}
