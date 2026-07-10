package com.bidhutkarki.tictactoe.user.dto;

public record AuthRegisterRequest(String email, String password) {

    public static AuthRegisterRequest from(RegisterRequest request) {
        return new AuthRegisterRequest(request.email(), request.password());
    }
}
