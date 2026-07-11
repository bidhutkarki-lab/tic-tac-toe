package com.bidhutkarki.tictactoe.user.controller;

import com.bidhutkarki.tictactoe.user.dto.ProfileResponse;
import com.bidhutkarki.tictactoe.user.dto.RegisterRequest;
import com.bidhutkarki.tictactoe.user.service.UserService;
import jakarta.validation.Valid;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<ProfileResponse> register(@Valid @RequestBody RegisterRequest request) {
        ProfileResponse response = userService.register(request);
        return ResponseEntity
                .created(URI.create("/users/" + response.id()))
                .body(response);
    }
}
