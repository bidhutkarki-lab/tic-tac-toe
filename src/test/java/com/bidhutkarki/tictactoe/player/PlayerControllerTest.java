package com.bidhutkarki.tictactoe.player;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class PlayerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void registersPlayerWithValidUsername() throws Exception {
        mockMvc.perform(post("/players")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\": \"alice\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username", is("alice")))
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    void rejectsDuplicateUsername() throws Exception {
        mockMvc.perform(post("/players")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\": \"bob\"}"))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/players")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\": \"BOB\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void rejectsBlankUsername() throws Exception {
        mockMvc.perform(post("/players")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\": \"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsTooShortUsername() throws Exception {
        mockMvc.perform(post("/players")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\": \"ab\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listsRegisteredPlayers() throws Exception {
        mockMvc.perform(post("/players")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\": \"charlie\"}"))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/players"))
                .andExpect(status().isOk());
    }
}
