package com.bidhutkarki.tictactoe.result.controller;

import com.bidhutkarki.tictactoe.result.GameResultService;
import com.bidhutkarki.tictactoe.result.dto.GameResultResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/results")
@RequiredArgsConstructor
public class GameResultController {

    private final GameResultService gameResultService;

    @GetMapping
    public List<GameResultResponse> list(
            @RequestParam(required = false) String playerId,
            @RequestParam(required = false) Long gameId) {
        if (playerId != null) {
            return gameResultService.findByPlayer(playerId);
        }
        if (gameId != null) {
            return gameResultService.findByGame(gameId);
        }
        return gameResultService.findAll();
    }
}
