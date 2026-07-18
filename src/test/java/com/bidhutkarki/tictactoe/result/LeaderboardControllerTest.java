package com.bidhutkarki.tictactoe.result;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bidhutkarki.tictactoe.player.entity.Player;
import com.bidhutkarki.tictactoe.player.repository.PlayerRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class LeaderboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private GameResultRepository gameResultRepository;

    @BeforeEach
    void clean() {
        gameResultRepository.deleteAll();
        playerRepository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        gameResultRepository.deleteAll();
        playerRepository.deleteAll();
    }

    @Test
    void ranksPlayersByEloDerivedFromAudit() throws Exception {
        Player alice = playerRepository.save(new Player("alice"));
        Player bob = playerRepository.save(new Player("bob"));

        // Game 1: alice beats bob. Both start at 1200, K=32, expected 0.5 each.
        // alice -> 1200 + 32*(1-0.5) = 1216, bob -> 1200 + 32*(0-0.5) = 1184.
        gameResultRepository.save(new GameResult(1L, alice.getId(), Outcome.WIN));
        gameResultRepository.save(new GameResult(1L, bob.getId(), Outcome.LOSS));

        mockMvc.perform(get("/leaderboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username", is("alice")))
                .andExpect(jsonPath("$[0].rank", is(1)))
                .andExpect(jsonPath("$[0].rating", is(1216)))
                .andExpect(jsonPath("$[0].wins", is(1)))
                .andExpect(jsonPath("$[0].gamesPlayed", is(1)))
                .andExpect(jsonPath("$[1].username", is("bob")))
                .andExpect(jsonPath("$[1].rank", is(2)))
                .andExpect(jsonPath("$[1].rating", is(1184)))
                .andExpect(jsonPath("$[1].losses", is(1)));
    }

    @Test
    void tiedRatingsShareRank() throws Exception {
        Player alice = playerRepository.save(new Player("alice"));
        Player bob = playerRepository.save(new Player("bob"));

        // A single draw leaves both at 1200 (expected 0.5, actual 0.5 -> no change).
        gameResultRepository.save(new GameResult(1L, alice.getId(), Outcome.DRAW));
        gameResultRepository.save(new GameResult(1L, bob.getId(), Outcome.DRAW));

        mockMvc.perform(get("/leaderboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].rating", is(1200)))
                .andExpect(jsonPath("$[0].rank", is(1)))
                .andExpect(jsonPath("$[1].rating", is(1200)))
                .andExpect(jsonPath("$[1].rank", is(1)));
    }
}
