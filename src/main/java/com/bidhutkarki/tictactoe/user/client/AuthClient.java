package com.bidhutkarki.tictactoe.user.client;

import com.bidhutkarki.tictactoe.user.dto.AuthRegisterRequest;
import com.bidhutkarki.tictactoe.user.dto.UserResponse;
import com.bidhutkarki.tictactoe.user.exception.AuthServiceException;
import com.bidhutkarki.tictactoe.user.exception.AuthServiceUnavailableException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

@Component
@Slf4j
@RequiredArgsConstructor
public class AuthClient {

    private static final String REGISTER_PATH = "/register";

    @Qualifier("authServiceRestClient")
    private final RestClient restClient;

    public UserResponse register(AuthRegisterRequest request) {
        log.info("Sending POST {} to auth service email={}", REGISTER_PATH, request.email());
        try {
            UserResponse response = restClient.post()
                    .uri(REGISTER_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
                        throw new AuthServiceUnavailableException(
                                "auth service returned " + res.getStatusCode());
                    })
                    .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                        throw new AuthServiceException(res.getStatusCode(), readBody(res));
                    })
                    .body(UserResponse.class);
            log.info("Auth service registered email={} authId={}", request.email(),
                    response != null ? response.id() : null);
            return response;
        } catch (ResourceAccessException ex) {
            throw new AuthServiceUnavailableException("auth service is unreachable", ex);
        }
    }

    private static String readBody(ClientHttpResponse response) {
        try {
            String body = new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8);
            return body.isBlank() ? "auth service rejected the request" : body;
        } catch (IOException ex) {
            return "auth service rejected the request";
        }
    }
}
