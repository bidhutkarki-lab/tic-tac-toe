package com.bidhutkarki.tictactoe.player.controller;

import com.bidhutkarki.tictactoe.common.security.AuthPrincipal;
import com.bidhutkarki.tictactoe.common.security.AuthUser;
import com.bidhutkarki.tictactoe.player.dto.PlayerResponse;
import com.bidhutkarki.tictactoe.player.dto.RegisterPlayerRequest;
import com.bidhutkarki.tictactoe.player.service.PlayerService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/players")
@RequiredArgsConstructor
public class PlayerController {

    private final PlayerService playerService;

    @PostMapping
    public ResponseEntity<PlayerResponse> register(
            @AuthPrincipal AuthUser principal, @Valid @RequestBody RegisterPlayerRequest request) {
        PlayerResponse response = playerService.register(principal.authId(), request);
        return ResponseEntity
                .created(URI.create("/players/" + response.id()))
                .body(response);
    }

    @GetMapping("/me")
    public PlayerResponse getCurrentPlayer(@AuthPrincipal AuthUser principal) {
        return playerService.findByUserId(principal.authId());
    }

    @GetMapping("/{id}")
    public PlayerResponse getPlayer(@PathVariable String id) {
        return playerService.findById(id);
    }

    @GetMapping
    public List<PlayerResponse> listPlayers() {
        return playerService.findAll();
    }
}
