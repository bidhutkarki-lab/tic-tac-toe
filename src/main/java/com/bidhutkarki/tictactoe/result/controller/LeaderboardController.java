package com.bidhutkarki.tictactoe.result.controller;

import com.bidhutkarki.tictactoe.result.LeaderboardService;
import com.bidhutkarki.tictactoe.result.dto.LeaderboardEntry;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/leaderboard")
@RequiredArgsConstructor
public class LeaderboardController {

    private final LeaderboardService leaderboardService;

    @GetMapping
    public List<LeaderboardEntry> leaderboard() {
        return leaderboardService.leaderboard();
    }
}
